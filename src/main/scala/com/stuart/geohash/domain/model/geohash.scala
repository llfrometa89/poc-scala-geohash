package com.stuart.geohash.domain.model

import io.estatico.newtype.macros.newtype

object geohash {

  @newtype case class Latitude(value: Double)
  @newtype case class Longitude(value: Double)
  @newtype case class GeoHashMaxPrecision(value: String)
  @newtype case class UniquePrefix(value: String)

  case class Point(latitude: Latitude, longitude: Longitude)

  case class GeoHash(point: Point, geohash: GeoHashMaxPrecision, uniquePrefix: UniquePrefix)
}
