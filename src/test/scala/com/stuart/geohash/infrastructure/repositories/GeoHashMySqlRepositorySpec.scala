package com.stuart.geohash.infrastructure.repositories

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.fixtures.{DoobieFixture, GeoHashFixture}
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import com.stuart.geohash.infrastructure.repositories.GeoHashSQL.GeoHashEntity
import doobie.ConnectionIO
import org.scalatest.OptionValues

class GeoHashMySqlRepositorySpec extends UnitSpec with GeoHashFixture with DoobieFixture with OptionValues {

  "create" should "create a GeoHash when it is not stored" in {
    val mySqlClient: MySqlClient[IO] = mock[MySqlClient[IO]]
    val geoHashSQL                   = mock[GeoHashSQL]
    val repository                   = GeoHashMySqlRepository.make(mySqlClient, geoHashSQL)

    when(geoHashSQL.insert(geoHash1)).thenReturn(1.pure[ConnectionIO])
    when(mySqlClient.transactor).thenReturn(IO(fakeTransactor))

    val result = repository.create(geoHash1).unsafeRunSync()
    result shouldBe geoHash1
  }

  "findBy" should "return a GeoHash when it is stored" in {

    val mySqlClient: MySqlClient[IO] = mock[MySqlClient[IO]]
    val geoHashSQL                   = mock[GeoHashSQL]
    val repository                   = GeoHashMySqlRepository.make(mySqlClient, geoHashSQL)

    when(geoHashSQL.selectByGeoHash(geoHash1.geoHash)).thenReturn(Option(geoHashEntity1).pure[ConnectionIO])
    when(mySqlClient.transactor).thenReturn(IO(fakeTransactor))

    val result = repository.findBy(geoHash1.geoHash).unsafeRunSync()
    result shouldBe defined
    result.value shouldBe geoHash1
  }
  it should "return a none when it is not stored" in {

    val mySqlClient: MySqlClient[IO] = mock[MySqlClient[IO]]
    val geoHashSQL                   = mock[GeoHashSQL]
    val repository                   = GeoHashMySqlRepository.make(mySqlClient, geoHashSQL)

    when(geoHashSQL.selectByGeoHash(geoHash1.geoHash)).thenReturn(Option.empty[GeoHashEntity].pure[ConnectionIO])
    when(mySqlClient.transactor).thenReturn(IO(fakeTransactor))

    val result = repository.findBy(geoHash1.geoHash).unsafeRunSync()
    result shouldBe empty
  }

  "findAll" should "return a list of GeoHash in the page and size given" in {
    val mySqlClient: MySqlClient[IO] = mock[MySqlClient[IO]]
    val geoHashSQL                   = mock[GeoHashSQL]
    val repository                   = GeoHashMySqlRepository.make(mySqlClient, geoHashSQL)

    val page: Long = 1L
    val size: Long = 10L

    when(geoHashSQL.selectAll(page, size)).thenReturn(List(geoHashEntity1).pure[ConnectionIO])
    when(mySqlClient.transactor).thenReturn(IO(fakeTransactor))

    val result = repository.findAll(page, size).unsafeRunSync()
    result should have size 1
    result shouldBe List(geoHash1)
  }

}
