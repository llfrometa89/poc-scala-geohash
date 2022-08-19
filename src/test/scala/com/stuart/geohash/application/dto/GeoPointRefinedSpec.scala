package com.stuart.geohash.application.dto

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.{GeoPointConversionError, GeoPointRefined}
import com.stuart.geohash.fixtures.GeoHashFixture

class GeoPointRefinedSpec extends UnitSpec with GeoHashFixture {

  "refine double value" should "return refined value" in {
    val computation = IO.fromEither(GeoPointRefined.fromDouble(90L))
    val result      = computation.unsafeRunSync()
    result.value shouldBe 90L
  }
  it should "return an conversion error when the latitude or longitude are invalid" in {
    val computation = IO.fromEither(GeoPointRefined.fromDouble(3000L))
    assertThrows[GeoPointConversionError] {
      computation.unsafeRunSync()
    }
  }

}
