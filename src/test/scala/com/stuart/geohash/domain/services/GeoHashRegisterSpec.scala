package com.stuart.geohash.domain.services

import cats.effect.IO
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.fixtures.GeoHashFixture
import cats.effect.unsafe.implicits.global

class GeoHashRegisterSpec extends UnitSpec with GeoHashFixture {

  "register a GeoHash" should "save a GeoHash when is not stored" in {
    val repo            = mock[GeoHashRepository[IO]]
    val geoHashRegister = GeoHashRegister.make(repo)

    when(repo.findBy(geoHash1.geoHash)).thenReturn(IO(None))
    when(repo.create(geoHash1)).thenReturn(IO(geoHash1))

    geoHashRegister.register(geoHash1).unsafeRunSync()

    verify(repo).findBy(geoHash1.geoHash)
    verify(repo).create(geoHash1)
  }
  it should "not save a GeoHash when is stored" in {
    val repo            = mock[GeoHashRepository[IO]]
    val geoHashRegister = GeoHashRegister.make(repo)

    when(repo.findBy(geoHash1.geoHash)).thenReturn(IO(Some(geoHash1)))

    geoHashRegister.register(geoHash1).unsafeRunSync()

    verify(repo).findBy(geoHash1.geoHash)
    verify(repo, never).create(geoHash1)
  }

}
