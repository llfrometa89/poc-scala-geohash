package com.stuart.geohash.application.services

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash._
import com.stuart.geohash.cross.GenGeoHash

import java.io.BufferedReader

trait ImportGeoHash[F[_]] {

  def importGeoHash(file: Resource[F, BufferedReader], batch: Long, precision: Int): F[List[GeoHashDTO]]
}

object ImportGeoHash {

  def make[F[_]: Sync: GenGeoHash]: ImportGeoHash[F] = new ImportGeoHash[F] {

    def importGeoHash(file: Resource[F, BufferedReader], batch: Long, precision: Int): F[List[GeoHashDTO]] =
      file.use { buffer =>
        for {
          lines     <- readBatch(buffer, batch)
          geoPoints <- lines.traverse(toImportGeoPoint)
          geoHashes <- geoPoints.traverse(gp => GenGeoHash[F].make(gp.toGeoPoint, 12))
          _         <- println(s"${lines.mkString("\n")}").pure[F]
        } yield List.empty[GeoHashDTO]

      }

    def toImportGeoPoint(line: String): F[ImportGeoPointDTO] =
      for {
        geoPoint          <- Sync[F].delay(line.split(","))
        latitudeAsDouble  <- Sync[F].delay(geoPoint(0).toDouble)
        longitudeAsDouble <- Sync[F].delay(geoPoint(1).toDouble)
        latitude          <- Sync[F].fromEither(GeoPointRefined.fromDouble(latitudeAsDouble))
        longitude         <- Sync[F].fromEither(GeoPointRefined.fromDouble(longitudeAsDouble))
      } yield ImportGeoPointDTO(latitude, longitude)

    private def readBatch(reader: BufferedReader, batch: Long): F[List[String]] = {

      def mkRead: F[Option[String]] = Sync[F].delay(reader.readLine()).map(Option(_))
      (1L to batch).toList.traverse(_ => mkRead).map(_.flatten)
    }
  }
}
