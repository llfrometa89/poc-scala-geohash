package com.stuart.geohash.application.services

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash._
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.domain.models.geohash.{GeoHash, GeoHashMaxPrecision, GeoPoint, UniquePrefix}
import com.stuart.geohash.domain.repositories.GeoHashRepository

import java.io.BufferedReader

trait ImportGeoHash[F[_]] {

  def importGeoHash(
    file: Resource[F, BufferedReader],
    batch: Long,
    precision: Int,
    onBatchFinish: List[GeoHashDTO] => F[Unit],
    onStart: => F[Unit],
    onFinish: => F[Unit]
  ): F[List[GeoHashDTO]]
}

object ImportGeoHash {

  def make[F[_]: Sync: GenGeoHash](repository: GeoHashRepository[F]): ImportGeoHash[F] = new ImportGeoHash[F] {

    final val MaxPrecision = 12

    def importGeoHash(
      file: Resource[F, BufferedReader],
      batch: Long,
      precision: Int,
      onBatchFinish: List[GeoHashDTO] => F[Unit],
      onStart: => F[Unit],
      onFinish: => F[Unit]
    ): F[List[GeoHashDTO]] = {

      def mkGeoHash(geoPoint: GeoPoint): F[GeoHash] = for {
        maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, MaxPrecision)
        uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
      } yield GeoHash(
        geoPoint,
        geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
        uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
      )

      def isBatchFinish(lines: List[String]) = lines.size < batch - 1

      def processGeoPoints(buffer: BufferedReader, list: List[GeoHashDTO]): F[List[GeoHashDTO]] = for {
        lines     <- readBatch(buffer, batch)
        geoPoints <- lines.traverse(toImportGeoPoint)
        geoHashes <- geoPoints.traverse(gp => mkGeoHash(gp.toGeoPoint))
        _         <- geoHashes.traverse(mkSaveGeoHash)
        geoHashesAsDto  = geoHashes.map(GeoHashDTO.fromGeoHash)
        geoHashesMerged = list ::: geoHashesAsDto
        accumulator <-
          if (isBatchFinish(lines)) geoHashesMerged.pure[F]
          else onBatchFinish(geoHashesAsDto) >> processGeoPoints(buffer, geoHashesMerged)
      } yield accumulator

      def mkSaveGeoHash(geoHash: GeoHash): F[Unit] = for {
        mGeoHash <- repository.findBy(geoHash.geoHash)
        _        <- Sync[F].whenA(mGeoHash.isEmpty)(repository.create(geoHash))
      } yield ()

      file.use { buffer =>
        for {
          _         <- onStart
          geoHashes <- processGeoPoints(buffer, List.empty[GeoHashDTO])
          _         <- onFinish
        } yield geoHashes
      }
    }

    private def toImportGeoPoint(line: String): F[ImportGeoPointDTO] =
      for {
        geoPointAsArray <- Sync[F].delay(line.toArrayByComma)
        geoPoint        <- GeoPointRefined.fromArray(geoPointAsArray)
      } yield geoPoint

    private def readBatch(reader: BufferedReader, batch: Long): F[List[String]] = {

      def avoidHeaderLine(line: Option[String]): Option[String] = line.filterNot(p => p == "lat,lng")

      def mkRead: F[Option[String]] = Sync[F].delay(reader.readLine()).map(Option(_)).map(avoidHeaderLine)
      (1L to batch).toList.traverse(_ => mkRead).map(_.flatten)
    }
  }
}
