package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.std.Console
import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.application.services.ImportGeoHash.{BatchResult, ExecutionResult}
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.output.{CsvFormatConsoleOutput, FormatConsoleOutput}
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput
import com.stuart.geohash.infrastructure.stdio.{CommandLineRunner, CommandOptions, CommandOptionsKeyword}
import org.apache.commons.cli.CommandLine
import com.stuart.geohash.cross.implicits._

import java.io.{BufferedReader, File, FileReader}
import scala.concurrent.duration.FiniteDuration

trait ImportCommand[F[_]] extends CommandLineRunner[F] {

  def run(args: Array[String]): F[Unit]
}

object ImportCommand {

  def make[F[_]: Sync: Clock: Console](
    commandOptions: CommandOptions[F],
    importGeoHash: ImportGeoHash[F],
    consoleOutput: ImportCommandFormatConsoleOutput[F],
    commandLineRunnerHelper: CommandLineRunnerHelper[F]
  ): ImportCommand[F] =
    new ImportCommand[F] {

      final val DefaultBatch     = 100
      final val DefaultPrecision = 5

      def run(args: Array[String]): F[Unit] = for {
        parser  <- commandLineRunnerHelper.parser
        options <- commandOptions.getOptions
        cmd     <- Sync[F].delay(parser.parse(options, args))
        hasFile <- Sync[F].delay(cmd.hasOption(CommandOptionsKeyword.file))
        _       <- Sync[F].whenA(hasFile)(mkImport(cmd))
      } yield ()

      private def openFile(filename: String): F[BufferedReader] = for {
        file       <- Sync[F].delay(new File(filename))
        fileReader <- Sync[F].delay(new FileReader(file))
        buffer     <- Sync[F].delay(new BufferedReader(fileReader))
      } yield buffer

      private def closeFile(file: BufferedReader): F[Unit] = Sync[F].delay(file.close())

      private def mkFileResource(filename: String): Resource[F, BufferedReader] =
        Resource.make(openFile(filename))(file => closeFile(file))

      private def mkImport(cmd: CommandLine): F[Unit] = for {
        filename      <- Sync[F].delay(cmd.getOptionValue(CommandOptionsKeyword.file))
        mBatch        <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.batch)).map(_.toInt))
        mPrecision    <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.precision)).map(_.toInt))
        format        <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.format)))
        batch         <- Sync[F].pure(mBatch.getOrElse(DefaultBatch))
        precision     <- Sync[F].pure(mPrecision.getOrElse(DefaultPrecision))
        onBatchFinish <- Sync[F].pure(onBatchFinish(format) _)
        onStart       <- Sync[F].pure(onStart(format) _)
        _ <- importGeoHash.importGeoHash(
          mkFileResource(filename),
          batch,
          precision,
          onBatchFinish,
          onStart,
          onFinish
        )
      } yield ()

      private def onStart(format: Option[String])(startTime: FiniteDuration): F[Unit] = for {
        _ <- Console[F].println(s"Starting process at ${startTime.formatDateTime}")
        _ <- printHeader(format)
      } yield ()

      private def onFinish(result: ExecutionResult): F[Unit] = for {
        _ <- Console[F].println(s"The import process has been finished at ${result.finishTime.formatDateTime}")
        _ <- Console[F].println("\n------------------------ Execution summary ------------------------ ")
        _ <- Console[F].println(s"Start time: ${result.startTime.formatDateTime}")
        _ <- Console[F].println(s"Finish time: ${result.finishTime.formatDateTime}")
        _ <- Console[F].println(s"Total lines: ${result.totalLines}")
        _ <- Console[F].println(s"Number of lines to process: ${result.totalToProcessLines}")
        _ <- Console[F].println(s"Count of batches: ${result.countOfBatches}")
        _ <- Console[F].println(s"Batch size: ${result.batchSize}")
        _ <- Console[F].println(s"Total execution time: ${(result.startTime, result.finishTime).elapsedTime}")
        _ <- Console[F].println(s"Count of errors: ${result.errorBatches.size}")
        _ <- Console[F].println("------------------------------------------------------------------- ")
      } yield ()

      private def onBatchFinish(format: Option[String])(batchResult: BatchResult): F[Unit] =
        for {
          geoHashes <- Sync[F].pure(batchResult.geoHashes.map(GeoHashDTO.fromGeoHash))
          _         <- consoleOutput.getConsoleOutputByFormat(format).printGeoHashes(geoHashes.toList)
        } yield ()

      private def printHeader(format: Option[String]): F[Unit] =
        format
          .map(FormatConsoleOutput(_))
          .collect { case CsvFormatConsoleOutput =>
            Console[F].println("lat,lng,geohash,uniq")
          }
          .sequence >> Sync[F].unit

    }
}
