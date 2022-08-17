package com.stuart.geohash.application.services

import com.stuart.geohash.application.dto.geohash.GeoHashDTO

import scala.reflect.io.File

trait ImportGeoHash {

  def importGeoHash(file: File): List[GeoHashDTO]
}
