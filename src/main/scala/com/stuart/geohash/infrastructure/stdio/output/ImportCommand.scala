package com.stuart.geohash.infrastructure.stdio.output

import cats.Monad
import cats.effect.std.Console
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.infrastructure.stdio.output.csv.ImportCommandConsoleCsvOutput
import com.stuart.geohash.infrastructure.stdio.output.json.ImportCommandConsoleJsonOutput

object ImportCommand {

  trait ImportCommandConsoleOutput[F[_], A] extends ConsoleOutput[A] {
    def printGeoHashes(geoHashes: List[A]): F[Unit]
  }

  trait ImportCommandFormatConsoleOutput[F[_]] {
    def getConsoleOutputByFormat(format: Option[String]): ImportCommandConsoleOutput[F, GeoHashDTO]
  }

  object ImportCommandFormatConsoleOutput {

    def make[F[_]: Monad: Console]: ImportCommandFormatConsoleOutput[F] = new ImportCommandFormatConsoleOutput[F] {

      def getConsoleOutputByFormat(format: Option[String]): ImportCommandConsoleOutput[F, GeoHashDTO] = {

        val csvOutput  = ImportCommandConsoleCsvOutput.make[F]
        val jsonOutput = ImportCommandConsoleJsonOutput.make[F]

        format
          .map(FormatConsoleOutput(_))
          .collect {
            case CsvFormatConsoleOutput  => csvOutput
            case JsonFormatConsoleOutput => jsonOutput
          }
          .getOrElse(csvOutput)
      }
    }
  }
}
