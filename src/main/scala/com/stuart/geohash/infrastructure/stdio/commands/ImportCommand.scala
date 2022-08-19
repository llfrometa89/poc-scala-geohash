package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.std.Console
import cats.effect.{Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput
import com.stuart.geohash.infrastructure.stdio.{CommandLineRunner, CommandOptions, CommandOptionsKeyword}
import org.apache.commons.cli.CommandLine

import java.io.{BufferedReader, File, FileReader}

trait ImportCommand[F[_]] extends CommandLineRunner[F] {

  def run(args: Array[String]): F[Unit]
}

object ImportCommand {

  def make[F[_]: Sync: Console](
    commandOptions: CommandOptions[F],
    importGeoHash: ImportGeoHash[F],
    consoleOutput: ImportCommandFormatConsoleOutput[F]
  ): ImportCommand[F] =
    new ImportCommand[F] {

      final val DefaultBatch     = 100L
      final val DefaultPrecision = 5

      def run(args: Array[String]): F[Unit] = for {
        parser  <- CommandLineRunnerHelper.parser[F]()
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
        mBatch        <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.batch)).map(_.toLong))
        mPrecision    <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.precision)).map(_.toInt))
        mFormat       <- Sync[F].delay(Option(cmd.getOptionValue(CommandOptionsKeyword.format)))
        batch         <- Sync[F].pure(mBatch.getOrElse(DefaultBatch))
        precision     <- Sync[F].pure(mPrecision.getOrElse(DefaultPrecision))
        onBatchFinish <- Sync[F].pure(onBatchFinish(mFormat) _)
        _ <- importGeoHash.importGeoHash(
          mkFileResource(filename),
          batch,
          precision,
          onBatchFinish,
          onStart,
          onFinish
        )
      } yield ()

      private def onStart: F[Unit] = Console[F].println("onStart: Starting process")

      private def onFinish: F[Unit] = Console[F].println("onFinish: imported process have been finish")

      private def onBatchFinish(format: Option[String])(geoHashes: List[GeoHashDTO]): F[Unit] =
        consoleOutput.getConsoleOutputByFormat(format).printGeoHashes(geoHashes)
    }
}
