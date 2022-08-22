package com.stuart.geohash.fixtures

import cats.effect.{IO, Resource}
import com.stuart.geohash.application.services.ImportGeoPointsFromFile.{BatchResult, ExecutionResult}
import com.stuart.geohash.domain.models.geohash
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.infrastructure.repositories.GeoHashSQL.GeoHashEntity

import java.io.BufferedReader
import scala.concurrent.duration.FiniteDuration

trait GeoHashFixture {

  val maxPresValue = Some(geohash.MaxPrecision)

  val lat1          = 41.388828145321
  val lon1          = 2.1689976634898
  val maxPres1      = "sp3e3qe7mkcb"
  val uniquePrefix1 = "sp3e3"

  val lat2          = 41.390743
  val lon2          = 2.1647467
  val maxPres2      = "sp3e2wuys9dr"
  val uniquePrefix2 = "sp3e3"

  val batchSize = 2
  val precision = Some(5)
  val line1     = "41.388828145321,2.1689976634898"
  val line2     = "41.390743,2.1647467"

  lazy val geoHash1 =
    GeoHash(GeoPoint(Latitude(lat1), Longitude(lon1)), GeoHashMaxPrecision(maxPres1), UniquePrefix(uniquePrefix1))

  lazy val geoHash2 =
    GeoHash(GeoPoint(Latitude(lat2), Longitude(lon2)), GeoHashMaxPrecision(maxPres2), UniquePrefix(uniquePrefix2))

  lazy val geoHashEntity1 = GeoHashEntity(lat1, lon1, maxPres1, uniquePrefix1)

  def mkBufferResource(f: BufferedReader): Resource[IO, BufferedReader] = Resource.make(IO(f))(_ => IO(f.close()))
  def onBatchFinish(r: BatchResult): IO[Unit]                           = IO.unit
  def onStart(fd: FiniteDuration): IO[Unit]                             = IO.unit
  def onFinish(r: ExecutionResult): IO[Unit]                            = IO.unit
}
