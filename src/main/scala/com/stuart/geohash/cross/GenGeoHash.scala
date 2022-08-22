package com.stuart.geohash.cross

import cats.effect.Sync
import cats.implicits._
import ch.hsr.geohash.GeoHash
import com.stuart.geohash.domain.models.geohash
import com.stuart.geohash.domain.models.geohash.{GeoPoint, Latitude, Longitude}

trait GenGeoHash[F[_]] {

  def make(point: GeoPoint, precision: Option[Int]): F[String]
}

object GenGeoHash {
  def apply[F[_]: GenGeoHash]: GenGeoHash[F] = implicitly

  implicit def instanceForSync[F[_]: Sync]: GenGeoHash[F] = new GenGeoHash[F] {

    val countOfDecimals = 1000.0

    def make(point: GeoPoint, precision: Option[Int]): F[String] = for {
      lat     <- point.latitude.value.pure[F]
      lon     <- point.longitude.value.pure[F]
      pre     <- precision.getOrElse(calculatePrecision(point)).pure[F]
      geoHash <- Sync[F].delay(GeoHash.geoHashStringWithCharacterPrecision(lat, lon, pre))
    } yield geoHash

    private def calculatePrecision(point: GeoPoint): Int =
      (1 to geohash.MaxPrecision).toList
        .flatMap { precision =>
          val gh             = GeoHash.withCharacterPrecision(point.latitude.value, point.longitude.value, precision)
          val geoHasAsString = gh.toBase32
          val ghDecode       = GeoHash.fromGeohashString(geoHasAsString)
          val boundingBox    = ghDecode.getBoundingBox.getCenter
          val center         = GeoPoint.fromDouble(boundingBox.getLatitude, boundingBox.getLongitude)
          val pointRound     = roundGeoPoint(point)
          val centerRound    = roundGeoPoint(center)
          if (equalsGeoPoint(pointRound, centerRound)) Some(precision) else None
        }
        .headOption
        .getOrElse(geohash.MaxPrecision)

    private def equalsGeoPoint(point: GeoPoint, pointTarget: GeoPoint): Boolean =
      point.latitude.value == pointTarget.latitude.value && point.longitude.value == pointTarget.longitude.value

    private def roundGeoPoint(point: GeoPoint): GeoPoint = {
      val roundLatitude  = Math.round(point.latitude.value * countOfDecimals) / countOfDecimals
      val roundLongitude = Math.round(point.longitude.value * countOfDecimals) / countOfDecimals
      GeoPoint.fromDouble(roundLatitude, roundLongitude)
    }
  }
}
