package com.stuart.geohash.application.dto

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval

object geohash {

  type LatLonRefined = Double Refined Interval.ClosedOpen[W.`-90.0d`.T, W.`90.0d`.T]
  type Latitude      = LatLonRefined
  type Longitude     = LatLonRefined

  case class ImportBrandDTO(latitude: Latitude, longitude: Longitude)

  case class GeoHashDTO(latitude: String, longitude: String, geohash: String, uniquePrefix: String)
}
