package com.stuart.geohash.application.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.GeoPointConversionError
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.domain.services.GeoPointLoader
import com.stuart.geohash.fixtures.GeoHashFixture

import java.io.BufferedReader

class ImportGeoHashSpec extends UnitSpec with GeoHashFixture {

  val batchSize = 2
  val precision = 5
  val line1     = "41.388828145321,2.1689976634898"
  val line2     = "41.390743,2.1647467"

  "import GeoPoint and convert to GeoHash" should "store all GeoHashes when they are not store" in {

    implicit val genGeoHash: GenGeoHash[IO] = mock[GenGeoHash[IO]]
    val geoHashRepository                   = mock[GeoHashRepository[IO]]
    val loader: GeoPointLoader[IO]          = mock[GeoPointLoader[IO]]
    val stub: ImportGeoHash[IO]             = ImportGeoHash.make(geoHashRepository, loader)
    val bufferedReader: BufferedReader      = mock[BufferedReader]
    val bufferResource                      = mkBufferResource(bufferedReader)

    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), 12)).thenReturn(IO.pure(maxPres1))
    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)).thenReturn(IO.pure(uniquePrefix1))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), 12)).thenReturn(IO.pure(maxPres2))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)).thenReturn(IO.pure(uniquePrefix2))
    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres1))).thenReturn(IO(None))
    when(geoHashRepository.create(geoHash1)).thenReturn(IO(geoHash1))
    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres2))).thenReturn(IO(None))
    when(geoHashRepository.create(geoHash2)).thenReturn(IO(geoHash2))
    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList(line1, line2)))

    stub.importGeoHash(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()

    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), 12)
    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), 12)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)
    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres1))
    verify(geoHashRepository).create(geoHash1)
    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres2))
    verify(geoHashRepository).create(geoHash2)
    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
  }
  it should "store not save the GeoHash when it is store" in {
    implicit val genGeoHash: GenGeoHash[IO] = mock[GenGeoHash[IO]]
    val geoHashRepository                   = mock[GeoHashRepository[IO]]
    val loader: GeoPointLoader[IO]          = mock[GeoPointLoader[IO]]
    val stub: ImportGeoHash[IO]             = ImportGeoHash.make(geoHashRepository, loader)
    val bufferedReader: BufferedReader      = mock[BufferedReader]
    val bufferResource                      = mkBufferResource(bufferedReader)

    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), 12)).thenReturn(IO.pure(maxPres1))
    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)).thenReturn(IO.pure(uniquePrefix1))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), 12)).thenReturn(IO.pure(maxPres2))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)).thenReturn(IO.pure(uniquePrefix2))
    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres1))).thenReturn(IO(Some(geoHash1)))
    when(geoHashRepository.create(geoHash1)).thenReturn(IO(geoHash1))
    when(geoHashRepository.findBy(GeoHashMaxPrecision(maxPres2))).thenReturn(IO(Some(geoHash2)))
    when(geoHashRepository.create(geoHash2)).thenReturn(IO(geoHash2))
    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList(line1, line2)))

    stub.importGeoHash(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()

    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), 12)
    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), 12)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)
    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres1))
    verify(geoHashRepository, never).create(geoHash1)
    verify(geoHashRepository).findBy(GeoHashMaxPrecision(maxPres2))
    verify(geoHashRepository, never).create(geoHash2)
    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
  }
  it should "throw an conversion error when GeoPoint is unknown value" in {

    implicit val genGeoHash: GenGeoHash[IO] = mock[GenGeoHash[IO]]
    val geoHashRepository                   = mock[GeoHashRepository[IO]]
    val loader: GeoPointLoader[IO]          = mock[GeoPointLoader[IO]]
    val stub: ImportGeoHash[IO]             = ImportGeoHash.make(geoHashRepository, loader)
    val bufferedReader: BufferedReader      = mock[BufferedReader]
    val bufferResource                      = mkBufferResource(bufferedReader)

    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList("unknown,unknown")))

    assertThrows[NumberFormatException] {
      stub.importGeoHash(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()
    }

    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
  }
  it should "throw an conversion error when GeoPoint is invalid" in {

    implicit val genGeoHash: GenGeoHash[IO] = mock[GenGeoHash[IO]]
    val geoHashRepository                   = mock[GeoHashRepository[IO]]
    val loader: GeoPointLoader[IO]          = mock[GeoPointLoader[IO]]
    val stub: ImportGeoHash[IO]             = ImportGeoHash.make(geoHashRepository, loader)
    val bufferedReader: BufferedReader      = mock[BufferedReader]
    val bufferResource                      = mkBufferResource(bufferedReader)

    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList("30000,30000")))

    assertThrows[GeoPointConversionError] {
      stub.importGeoHash(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()
    }

  }
}
