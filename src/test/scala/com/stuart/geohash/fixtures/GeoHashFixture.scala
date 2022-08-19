package com.stuart.geohash.fixtures

import com.stuart.geohash.domain.models.geohash.{
  GeoHash,
  GeoHashMaxPrecision,
  GeoPoint,
  Latitude,
  Longitude,
  UniquePrefix
}

trait GeoHashFixture {

  val lat1          = 41.388828145321
  val lon1          = 2.1689976634898
  val maxPres1      = "sp3e3qe7mkcb"
  val uniquePrefix1 = "sp3e3"

  val lat2          = 41.390743
  val lon2          = 2.1647467
  val maxPres2      = "sp3e2wuys9dr"
  val uniquePrefix2 = "sp3e3"

  lazy val geoHash1 =
    GeoHash(GeoPoint(Latitude(lat1), Longitude(lon1)), GeoHashMaxPrecision(maxPres1), UniquePrefix(uniquePrefix1))

  lazy val geoHash2 =
    GeoHash(GeoPoint(Latitude(lat2), Longitude(lon2)), GeoHashMaxPrecision(maxPres2), UniquePrefix(uniquePrefix2))
}
