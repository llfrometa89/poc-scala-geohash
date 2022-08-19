package com.stuart.geohash.domain.repositories

import cats.effect.IO
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.domain.models.geohash.GeoHashMaxPrecision
import com.stuart.geohash.domain.services.GeoHashRegister
import com.stuart.geohash.fixtures.GeoHashFixture
import cats.effect.unsafe.implicits.global

class GeoHashRegisterSpec extends UnitSpec with GeoHashFixture {

  "register GeoHash" should "save GeoHash when it is not store" in {
    val geoHashRepository: GeoHashRepository[IO] = mock[GeoHashRepository[IO]]
    val geoHashRegister                          = GeoHashRegister.make(geoHashRepository)

    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres1))).thenReturn(IO(None))
    when(geoHashRepository.create(geoHash1)).thenReturn(IO(geoHash1))

    geoHashRegister.register(geoHash1).unsafeRunSync()

    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres1))
    verify(geoHashRepository).create(geoHash1)
  }
  it should "no save GeoHash when it is store" in {
    val geoHashRepository: GeoHashRepository[IO] = mock[GeoHashRepository[IO]]
    val geoHashRegister                          = GeoHashRegister.make(geoHashRepository)

    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres1))).thenReturn(IO(Some(geoHash1)))

    geoHashRegister.register(geoHash1).unsafeRunSync()

    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres1))
    verify(geoHashRepository, never).create(geoHash1)
  }
}
