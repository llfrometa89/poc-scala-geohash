package com.stuart.geohash.domain.service

import com.stuart.geohash.domain.model.geohash.Point

trait GenGeoHash[F[_]] {

  def make(point: Point, precision: Int): F[String]

}
