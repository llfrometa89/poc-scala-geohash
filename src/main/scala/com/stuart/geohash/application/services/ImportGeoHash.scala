package com.stuart.geohash.application.services

import cats.Parallel
import cats.effect.{Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash._
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.domain.models.geohash.{GeoHash, GeoHashMaxPrecision, GeoPoint, UniquePrefix}
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.domain.services.GeoPointLoader

import java.io.BufferedReader

trait ImportGeoHash[F[_]] {

  def importGeoHash(
    file: Resource[F, BufferedReader],
    batch: Int,
    precision: Int,
    onBatchFinish: List[GeoHashDTO] => F[Unit],
    onStart: => F[Unit],
    onFinish: => F[Unit]
  ): F[Unit]
}

object ImportGeoHash {

  def make[F[_]: Sync: Parallel: GenGeoHash](repo: GeoHashRepository[F], loader: GeoPointLoader[F]): ImportGeoHash[F] =
    new ImportGeoHash[F] {

      final val MaxPrecision = 12

      def importGeoHash(
        file: Resource[F, BufferedReader],
        batchSize: Int,
        precision: Int,
        onBatchFinish: List[GeoHashDTO] => F[Unit],
        onStart: => F[Unit],
        onFinish: => F[Unit]
      ): F[Unit] = {

        def processGeoPointsByBatch(lines: LazyList[String]): F[Unit] = for {
          geoPoints <- lines.traverse(GeoPointRefined.fromLine[F])
          geoHashes <- geoPoints.traverse(gp => mkGeoHash(gp.toGeoPoint, precision))
          _         <- geoHashes.parTraverse_(mkSaveGeoHash)
          geoHashesAsDto = geoHashes.map(GeoHashDTO.fromGeoHash)
          _ <- onBatchFinish(geoHashesAsDto.toList)
        } yield ()

        def processGeoPoints(batches: List[LazyList[String]]): F[Unit] =
          batches match {
            case Nil          => Sync[F].unit
            case head :: tail => processGeoPointsByBatch(head) >> processGeoPoints(tail)
          }

        file.use { buffer =>
          for {
            _     <- onStart
            lines <- loader.load(buffer, batchSize, LazyList.empty[String])
            batches = lines.distinct.grouped(batchSize).toList
            _ <- processGeoPoints(batches)
            _ <- onFinish
          } yield ()
        }
      }

      private def mkSaveGeoHash(geoHash: GeoHash): F[Unit] = for {
        mGeoHash <- repo.findBy(geoHash.geoHash)
        _        <- Sync[F].whenA(mGeoHash.isEmpty)(repo.create(geoHash))
      } yield ()

      private def mkGeoHash(geoPoint: GeoPoint, precision: Int): F[GeoHash] = for {
        maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, MaxPrecision)
        uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
      } yield GeoHash(
        geoPoint,
        geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
        uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
      )

    }
}
