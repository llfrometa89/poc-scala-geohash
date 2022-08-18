package com.stuart.geohash.application.dto

import cats.implicits._
import com.stuart.geohash.domain.model.geohash.{GeoPoint, Latitude, Longitude}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.{refineV, W}

object geohash {

  type GeoPointPred = Interval.ClosedOpen[W.`-90.0d`.T, W.`90.0d`.T]
  type Latitude     = Double Refined GeoPointPred
  type Longitude    = Double Refined GeoPointPred

  object GeoPointRefined {

    def fromDouble(value: Double): Either[GeoPointError, Refined[Double, GeoPointPred]] =
      refineV[GeoPointPred](value).leftMap(GeoPointConversionError)
  }

  case class ImportGeoPointDTO(latitude: Latitude, longitude: Longitude) {
    def toGeoPoint: GeoPoint = GeoPoint(
      latitude = Latitude(latitude.value),
      longitude = Longitude(longitude.value)
    )
  }

  case class GeoHashDTO(latitude: String, longitude: String, geohash: String, uniquePrefix: String)

  abstract class GeoPointError                        extends Exception
  case class GeoPointConversionError(message: String) extends GeoPointError
}
