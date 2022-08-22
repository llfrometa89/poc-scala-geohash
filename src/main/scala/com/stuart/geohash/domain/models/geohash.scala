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

  object GeoPoint {
    def fromDouble(latitude: Double, longitude: Double): GeoPoint =
      GeoPoint(latitude = Latitude(latitude), longitude = Longitude(longitude))
  }

  object GeoHash {

    trait GeoHashError                                extends Exception
    case class GeoHashExecutionError(message: String) extends GeoHashError

    def make[F[_]: Sync: GenGeoHash](geoPoint: GeoPoint, precision: Option[Int]): F[GeoHash] =
      for {
        maxPrecisionGeoHash <- GenGeoHash[F].make(geoPoint, Some(MaxPrecision))
        uniquePrefixGeoHash <- GenGeoHash[F].make(geoPoint, precision)
      } yield GeoHash(
        geoPoint,
        geoHash = GeoHashMaxPrecision(maxPrecisionGeoHash),
        uniquePrefix = UniquePrefix(uniquePrefixGeoHash)
      )
  }
}
