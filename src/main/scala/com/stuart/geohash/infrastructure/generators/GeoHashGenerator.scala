package com.stuart.geohash.infrastructure.generators

import cats.effect.Sync
import cats.implicits._
import ch.hsr.geohash.GeoHash
import com.stuart.geohash.domain.model.geohash.Point
import com.stuart.geohash.domain.service.GenGeoHash

object GeoHashGenerator {

  def apply[F[_]: GenGeoHash]: GenGeoHash[F] = implicitly

  def instanceForSync[F[_]: Sync](): GenGeoHash[F] = new GenGeoHash[F] {

    def make(point: Point, precision: Int): F[String] = for {
      lat     <- Sync[F].pure(point.longitude.value)
      lon     <- Sync[F].pure(point.longitude.value)
      pre     <- Sync[F].pure(precision)
      geohash <- Sync[F].delay(GeoHash.geoHashStringWithCharacterPrecision(lat, lon, pre))
    } yield geohash
  }
}
