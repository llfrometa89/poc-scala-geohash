package com.stuart.infrastructure.repositories

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.fixtures.GeoHashFixtureIT
import com.stuart.geohash.domain.models.geohash.GeoHashMaxPrecision
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.infrastructure.repositories.{GeoHashMySqlRepository, GeoHashSQL}
import com.stuart.{IntegrationSpec, MySqlTestingConnection, TestContainer}
import org.scalatest.OptionValues

class GeoHashMysqlRepositoryITSpec
    extends IntegrationSpec
    with TestContainer
    with MySqlTestingConnection
    with GeoHashFixtureIT
    with OptionValues {

  override def beforeAll(): Unit = initializeDatabase()

  before {
    cleanDatabase()
  }

  "create" should "create a GeoHash when it is not stored" in {
    val computation = for {
      repo    <- getRepository
      geoHash <- repo.create(geoHash1)
    } yield geoHash

    val result = computation.unsafeRunSync()
    result shouldBe geoHash1
  }

  "findBy" should "return a GeoHash when it is stored" in {
    val computation = for {
      repo         <- getRepository
      savedGeoHash <- repo.create(geoHash1)
      geoHash      <- repo.findBy(savedGeoHash.geoHash)
    } yield geoHash

    val result = computation.unsafeRunSync()
    result.value shouldBe geoHash1
  }
  it should "return a none when it is not stored" in {

    val computation = for {
      repo    <- getRepository
      geoHash <- repo.findBy(GeoHashMaxPrecision("unknown"))
    } yield geoHash

    val result = computation.unsafeRunSync()
    result shouldBe empty
  }

  "findAll" should "return a list of GeoHash in the page and size given" in {

    val computation = for {
      repo      <- getRepository
      _         <- repo.create(geoHash1)
      _         <- repo.create(geoHash2)
      geoHashes <- repo.findAll(0, 5)
    } yield geoHashes

    val result = computation.unsafeRunSync()
    result should have size 2
    result shouldBe List(geoHash2, geoHash1)
  }

  def getRepository: IO[GeoHashRepository[IO]] = for {
    mySqlClient <- mySqlClientTest
    repository  <- IO.delay(GeoHashMySqlRepository.make[IO](mySqlClient, GeoHashSQL.make))
  } yield repository

  def cleanDatabase(): Unit =
    getRepository.flatMap(_.deleteAll()).unsafeRunSync()
}
