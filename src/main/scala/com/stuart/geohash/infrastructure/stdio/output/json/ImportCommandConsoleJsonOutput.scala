package com.stuart.geohash.infrastructure.stdio.output.json

import cats.effect.std.Console
import cats.implicits._
import cats.{Monad, Show}
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandConsoleOutput
import io.circe.syntax._

trait ImportCommandConsoleJsonOutput[F[_], A] extends ImportCommandConsoleOutput[F, A]

object ImportCommandConsoleJsonOutput {

  def make[F[_]: Monad: Console]: ImportCommandConsoleJsonOutput[F, GeoHashDTO] =
    new ImportCommandConsoleJsonOutput[F, GeoHashDTO] {
      def printGeoHashes(geoHashes: List[GeoHashDTO]): F[Unit] = for {
        _ <- Console[F].println("[")
        _ <- geoHashes.traverse(geoHash => Console[F].print(geoHash)(show) *> Console[F].println(","))
        _ <- Console[F].println("]")
      } yield ()

      def show: Show[GeoHashDTO] =
        Show.show(geoHash => geoHash.asJson.toString())
    }
}
