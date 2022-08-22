package com.stuart.geohash.application.dto

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.{GeoPointRefined, ImportGeoPointDTO}
import com.stuart.geohash.domain.models.geohash.{GeoPoint, Latitude, Longitude}
import com.stuart.geohash.fixtures.GeoHashFixture

class ImportGeoPointDtoSpec extends UnitSpec with GeoHashFixture {

  "convert to GeoHash" should "return valid GeoPoint refined object" in {

    val computation = for {
      lat1R <- IO.fromEither(GeoPointRefined.fromLatitudeAsDouble(lat1))
      lon1  <- IO.fromEither(GeoPointRefined.fromLongitudeAsDouble(lon1))
    } yield ImportGeoPointDTO(lat1R, lon1).toGeoPoint

    val result = computation.unsafeRunSync()

    result shouldBe GeoPoint(
      latitude = Latitude(lat1),
      longitude = Longitude(lon1)
    )
  }

}
