package com.stuart.geohash.infrastructure.stdio.output.csv

import cats.{Monad, Show}
import cats.effect.std.Console
import cats.implicits._
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandConsoleOutput

trait ImportCommandConsoleCsvOutput[F[_], A] extends ImportCommandConsoleOutput[F, A]

object ImportCommandConsoleCsvOutput {

  def make[F[_]: Monad: Console]: ImportCommandConsoleCsvOutput[F, GeoHashDTO] =
    new ImportCommandConsoleCsvOutput[F, GeoHashDTO] {

      def show: Show[GeoHashDTO] =
        Show.show(geoHash => s"${geoHash.latitude},${geoHash.longitude},${geoHash.geoHash},${geoHash.uniquePrefix}")

      def printGeoHashes(geoHashes: List[GeoHashDTO]): F[Unit] = for {
        _ <- Console[F].println("lat,lng,geohash,uniq")
        _ <- geoHashes.traverse(geoHash => Console[F].println(geoHash)(show))
      } yield ()
    }
}
