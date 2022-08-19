package com.stuart.geohash.domain.models

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.cross.GenGeoHash
import io.estatico.newtype.macros.newtype

object geohash {

  final val MaxPrecision = 12

  @newtype case class Latitude(value: Double)
  @newtype case class Longitude(value: Double)
  @newtype case class GeoHashMaxPrecision(value: String)
  @newtype case class UniquePrefix(value: String)

  case class GeoPoint(latitude: Latitude, longitude: Longitude)

  sealed case class GeoHash private (
    geoPoint: GeoPoint,
    geoHash: GeoHashMaxPrecision,
    uniquePrefix: UniquePrefix
  )

  object GeoHash {

    def make[F[_]: Sync: GenGeoHash](geoPoint: GeoPoint, precision: Int): F[GeoHash] =
      for {
        maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, MaxPrecision)
        uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
      } yield GeoHash(
        geoPoint,
        geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
        uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
      )
  }
}
