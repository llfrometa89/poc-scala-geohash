package com.stuart.geohash.application.services

import cats.Parallel
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
  ): F[Unit]
}

object ImportGeoHash {

  def make[F[_]: Sync: Parallel: GenGeoHash](repository: GeoHashRepository[F]): ImportGeoHash[F] =
    new ImportGeoHash[F] {

      final val MaxPrecision = 12

      def importGeoHash(
        file: Resource[F, BufferedReader],
        batch: Long,
        precision: Int,
        onBatchFinish: List[GeoHashDTO] => F[Unit],
        onStart: => F[Unit],
        onFinish: => F[Unit]
      ): F[Unit] = {

        def mkGeoHash(geoPoint: GeoPoint): F[GeoHash] = for {
          maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, MaxPrecision)
          uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
        } yield GeoHash(
          geoPoint,
          geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
          uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
        )

        def isBatchFinish(lines: LazyList[String]) = lines.size < batch - 1

        def mkSaveGeoHash(geoHash: GeoHash): F[Unit] = for {
          mGeoHash <- repository.findBy(geoHash.geoHash)
          _        <- Sync[F].whenA(mGeoHash.isEmpty)(repository.create(geoHash))
        } yield ()

        def processGeoPoints(lines: List[LazyList[String]]): F[Unit] =
          lines match {
            case Nil          => Sync[F].unit
            case head :: tail => processGeoPointsByBatch(head) >> processGeoPoints(tail)
          }

        def processGeoPointsByBatch(lines: LazyList[String]): F[Unit] = for {
          geoPoints <- lines.traverse(toImportGeoPoint)
          geoHashes <- geoPoints.traverse(gp => mkGeoHash(gp.toGeoPoint))
          _         <- geoHashes.parTraverse_(mkSaveGeoHash)
          geoHashesAsDto = geoHashes.map(GeoHashDTO.fromGeoHash)
          _ <- onBatchFinish(geoHashesAsDto.toList)
        } yield ()

        def filterDuplicateGeoPoints(buffer: BufferedReader, list: LazyList[String]): F[LazyList[String]] = for {
          lines <- readBatch(buffer, batch)
          accumulatorBatch = (list #::: lines).distinct
          accumulator <-
            if (isBatchFinish(lines)) Sync[F].unit >> Sync[F].pure(accumulatorBatch)
            else filterDuplicateGeoPoints(buffer, accumulatorBatch)
        } yield accumulator

        def mkBatches(lines: LazyList[String]): List[LazyList[String]] = lines.distinct.grouped(batch.toInt).toList

        file.use { buffer =>
          for {
            _     <- onStart
            lines <- filterDuplicateGeoPoints(buffer, LazyList.empty[String])
            linesInBatches = mkBatches(lines)
            _ <- processGeoPoints(linesInBatches)
            _ <- onFinish
          } yield ()
        }
      }

      private def toImportGeoPoint(line: String): F[ImportGeoPointDTO] =
        for {
          geoPointAsArray <- Sync[F].delay(line.toArrayByComma)
          geoPoint        <- GeoPointRefined.fromArray(geoPointAsArray)
        } yield geoPoint

      private def readBatch(reader: BufferedReader, batch: Long): F[LazyList[String]] = {

        def avoidHeaderLine(line: Option[String]): Option[String] = line.filterNot(p => p == "lat,lng")

        def mkRead: F[Option[String]] = Sync[F].delay(reader.readLine()).map(Option(_)).map(avoidHeaderLine)
        (1L to batch).to(LazyList).traverse(_ => mkRead).map(_.flatten)
      }
    }
}
