package com.stuart.geohash.application.dto

import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.fixtures.GeoHashFixture

class GeoHashDtoSpec extends UnitSpec with GeoHashFixture {

  "convert to GeoHashDTO from GeoHash" should "return valid GeoHashDTO object" in {

    val result = GeoHashDTO.fromGeoHash(geoHash1)

    result shouldBe GeoHashDTO(
      latitude = lat1,
      longitude = lon1,
      geoHash = maxPres1,
      uniquePrefix = uniquePrefix1
    )
  }

}
