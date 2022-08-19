package com.stuart.geohash.domain.services

import java.io.BufferedReader

trait GeoPointLoader[F[_]] {
  def load(buffer: BufferedReader, batchSize: Int, accumulatorLines: LazyList[String]): F[LazyList[String]]
}
