package com.stuart.geohash.cross

import cats.effect.Sync
import cats.implicits._
import ch.hsr.geohash.GeoHash
import com.stuart.geohash.domain.models.geohash.GeoPoint

trait GenGeoHash[F[_]] {

  def make(point: GeoPoint, precision: Int): F[String]
}

object GenGeoHash {
  def apply[F[_]: GenGeoHash]: GenGeoHash[F] = implicitly

  implicit def instanceForSync[F[_]: Sync]: GenGeoHash[F] = new GenGeoHash[F] {

    def make(point: GeoPoint, precision: Int): F[String] = for {
      lat     <- point.longitude.value.pure[F]
      lon     <- point.longitude.value.pure[F]
      pre     <- precision.pure[F]
      geohash <- Sync[F].delay(GeoHash.geoHashStringWithCharacterPrecision(lat, lon, pre))
    } yield geohash
  }
}
