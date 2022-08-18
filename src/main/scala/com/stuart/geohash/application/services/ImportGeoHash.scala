package com.stuart.geohash.application.services

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash._
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.domain.model.geohash.{GeoHash, GeoHashMaxPrecision, GeoPoint, UniquePrefix}

import java.io.BufferedReader

trait ImportGeoHash[F[_]] {

  def importGeoHash(file: Resource[F, BufferedReader], batch: Long, precision: Int): F[List[GeoHashDTO]]
}

object ImportGeoHash {

  def make[F[_]: Sync: GenGeoHash]: ImportGeoHash[F] = new ImportGeoHash[F] {

    def importGeoHash(file: Resource[F, BufferedReader], batch: Long, precision: Int): F[List[GeoHashDTO]] = {

      def mkGeoHash(geoPoint: GeoPoint): F[GeoHash] = for {
        maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, 12)
        uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
      } yield GeoHash(
        geoPoint,
        geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
        uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
      )

      file.use { buffer =>
        for {
          lines     <- readBatch(buffer, batch)
          geoPoints <- lines.traverse(toImportGeoPoint)
          geoHashes <- geoPoints.traverse(gp => mkGeoHash(gp.toGeoPoint))
        } yield geoHashes.map(GeoHashDTO.fromGeoHash)

      }
    }

    def toImportGeoPoint(line: String): F[ImportGeoPointDTO] =
      for {
        geoPointAsArray <- Sync[F].delay(line.split(","))
        geoPoint        <- GeoPointRefined.fromArray(geoPointAsArray)
      } yield geoPoint

    private def readBatch(reader: BufferedReader, batch: Long): F[List[String]] = {

      def mkRead: F[Option[String]] = Sync[F].delay(reader.readLine()).map(Option(_))
      (1L to batch).toList.traverse(_ => mkRead).map(_.flatten)
    }
  }
}
