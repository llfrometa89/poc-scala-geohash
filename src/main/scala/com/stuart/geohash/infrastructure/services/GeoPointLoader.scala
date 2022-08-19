package com.stuart.geohash.infrastructure.services

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.domain.services.GeoPointLoader

import java.io.BufferedReader

object GeoPointLoader {

  def make[F[_]: Sync](): GeoPointLoader[F] = new GeoPointLoader[F] {

    def load(buffer: BufferedReader, batchSize: Int, accumulatorLines: LazyList[String]): F[LazyList[String]] =
      for {
        lines <- readBatch(buffer, batchSize)
        mergedLines = accumulatorLines #::: lines
        accumulator <-
          if (isBatchFinish(lines.size, batchSize)) Sync[F].unit >> Sync[F].pure(mergedLines)
          else load(buffer, batchSize, mergedLines)
      } yield accumulator

    private def isBatchFinish(numberOfLine: Int, batchSize: Int): Boolean = numberOfLine < batchSize - 1

    private def avoidHeaderLine(line: Option[String]): Option[String] = line.filterNot(p => p == "lat,lng")

    private def readBatch(reader: BufferedReader, batchSize: Int): F[LazyList[String]] = {
      def mkRead: F[Option[String]] = Sync[F].delay(reader.readLine()).map(Option(_)).map(avoidHeaderLine)
      (1 to batchSize).to(LazyList).traverse(_ => mkRead).map(_.flatten)
    }
  }

}
