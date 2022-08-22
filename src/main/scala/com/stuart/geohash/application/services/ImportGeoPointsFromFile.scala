package com.stuart.geohash.application.services

import cats.Parallel
import cats.effect.{Clock, Resource, Sync}
import cats.implicits._
import com.stuart.geohash.application.dto.geohash._
import com.stuart.geohash.application.services.ImportGeoPointsFromFile.{BatchResult, ExecutionResult}
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.domain.models.geohash.GeoHash
import com.stuart.geohash.domain.services.{GeoHashRegister, GeoPointLoader}

import java.io.BufferedReader
import scala.concurrent.duration.FiniteDuration

trait ImportGeoPointsFromFile[F[_]] {

  def importGeoPoints(
    file: Resource[F, BufferedReader],
    batch: Int,
    precision: Option[Int],
    onBatchFinish: BatchResult => F[Unit],
    onStart: FiniteDuration => F[Unit],
    onFinish: ExecutionResult => F[Unit]
  ): F[Unit]
}

object ImportGeoPointsFromFile {

  def make[F[_]: Sync: Clock: Parallel: GenGeoHash](
    loader: GeoPointLoader[F],
    geoHashRegister: GeoHashRegister[F]
  ): ImportGeoPointsFromFile[F] = new ImportGeoPointsFromFile[F] {

    def importGeoPoints(
      file: Resource[F, BufferedReader],
      batchSize: Int,
      precision: Option[Int],
      onBatchFinish: BatchResult => F[Unit],
      onStart: FiniteDuration => F[Unit],
      onFinish: ExecutionResult => F[Unit]
    ): F[Unit] = {

      def mkParallelSaveExecution(geoHashes: LazyList[GeoHash]): F[BatchResult] =
        for {
          occurredOn <- Clock[F].realTime
          batchResult <- geoHashes
            .parTraverse_(geoHashRegister.register)
            .map(_ => BatchResult(geoHashes, hasError = false, occurredOn))
            .handleErrorWith(_ => Sync[F].pure(BatchResult(geoHashes, hasError = true, occurredOn)))
        } yield batchResult

      def mkImportGeoPointsByBatch(lines: LazyList[String]): F[BatchResult] = for {
        geoPoints   <- lines.traverse(GeoPointRefined.fromLine[F])
        geoHashes   <- geoPoints.traverse(gp => GeoHash.make(gp.toGeoPoint, precision))
        batchResult <- mkParallelSaveExecution(geoHashes)
        _           <- onBatchFinish(batchResult)
      } yield batchResult

      def mkImportGeoPoints(batches: List[LazyList[String]], result: List[BatchResult]): F[List[BatchResult]] =
        batches match {
          case Nil => Sync[F].pure(result)
          case head :: tail =>
            for {
              batchResult <- mkImportGeoPointsByBatch(head)
              accumulator <- mkImportGeoPoints(tail, batchResult :: result)
            } yield accumulator
        }

      file.use { buffer =>
        for {
          startTime  <- Clock[F].realTime
          _          <- onStart(startTime)
          totalLines <- loader.load(buffer, batchSize, LazyList.empty[String])
          totalToProcessLines = totalLines.distinct
          batches             = totalToProcessLines.grouped(batchSize).toList
          batchResult <- mkImportGeoPoints(batches, List.empty[BatchResult])
          endTime     <- Clock[F].realTime
          executionResult = ExecutionResult(
            totalLines.size,
            totalToProcessLines.size,
            batches.size,
            batchSize,
            startTime,
            endTime,
            batchResult.filter(_.hasError)
          )
          _ <- onFinish(executionResult)
        } yield ()
      }
    }
  }

  case class BatchResult(geoHashes: LazyList[GeoHash], hasError: Boolean, occurredOn: FiniteDuration)

  case class ExecutionResult(
    totalLines: Int,
    totalToProcessLines: Int,
    countOfBatches: Int,
    batchSize: Int,
    startTime: FiniteDuration,
    finishTime: FiniteDuration,
    errorBatches: List[BatchResult]
  ) {
    def duplicatedLines: Int = totalLines - totalToProcessLines
  }
}
