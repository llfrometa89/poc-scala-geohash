package com.stuart.geohash.application.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.GeoPointConversionError
import com.stuart.geohash.cross.GenGeoHash
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.domain.services.{GeoHashRegister, GeoPointLoader}
import com.stuart.geohash.fixtures.GeoHashFixture

import java.io.BufferedReader

class ImportGeoPointsFromFileSpec extends UnitSpec with GeoHashFixture {

  "import GeoPoint and convert to GeoHash" should "store all GeoHashes when they are not store" in {

    implicit val genGeoHash: GenGeoHash[IO]  = mock[GenGeoHash[IO]]
    val loader: GeoPointLoader[IO]           = mock[GeoPointLoader[IO]]
    val geoHashRegister: GeoHashRegister[IO] = mock[GeoHashRegister[IO]]
    val stub: ImportGeoPointsFromFile[IO]    = ImportGeoPointsFromFile.make(loader, geoHashRegister)
    val bufferedReader: BufferedReader       = mock[BufferedReader]
    val bufferResource                       = mkBufferResource(bufferedReader)

    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), maxPresValue)).thenReturn(IO.pure(maxPres1))
    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)).thenReturn(IO.pure(uniquePrefix1))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), maxPresValue)).thenReturn(IO.pure(maxPres2))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)).thenReturn(IO.pure(uniquePrefix2))
    when(geoHashRegister.register(geoHash1)).thenReturn(IO.unit)
    when(geoHashRegister.register(geoHash2)).thenReturn(IO.unit)
    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList(line1, line2)))

    stub.importGeoPoints(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()

    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), maxPresValue)
    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), maxPresValue)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)
    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
    verify(geoHashRegister).register(geoHash1)
    verify(geoHashRegister).register(geoHash2)
  }
  it should "store not save the GeoHash when it is store" in {
    implicit val genGeoHash: GenGeoHash[IO]  = mock[GenGeoHash[IO]]
    val loader: GeoPointLoader[IO]           = mock[GeoPointLoader[IO]]
    val geoHashRegister: GeoHashRegister[IO] = mock[GeoHashRegister[IO]]
    val stub: ImportGeoPointsFromFile[IO]    = ImportGeoPointsFromFile.make(loader, geoHashRegister)
    val bufferedReader: BufferedReader       = mock[BufferedReader]
    val bufferResource                       = mkBufferResource(bufferedReader)

    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), maxPresValue)).thenReturn(IO.pure(maxPres1))
    when(genGeoHash.make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)).thenReturn(IO.pure(uniquePrefix1))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), maxPresValue)).thenReturn(IO.pure(maxPres2))
    when(genGeoHash.make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)).thenReturn(IO.pure(uniquePrefix2))
    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList(line1, line2)))
    when(geoHashRegister.register(geoHash1)).thenReturn(IO.unit)
    when(geoHashRegister.register(geoHash2)).thenReturn(IO.unit)

    stub.importGeoPoints(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()

    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), maxPresValue)
    verify(genGeoHash).make(GeoPoint(Latitude(lat1), Longitude(lon1)), precision)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), maxPresValue)
    verify(genGeoHash).make(GeoPoint(Latitude(lat2), Longitude(lon2)), precision)
    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
    verify(geoHashRegister).register(geoHash1)
    verify(geoHashRegister).register(geoHash2)
  }
  it should "throw an conversion error when GeoPoint is unknown value" in {

    implicit val genGeoHash: GenGeoHash[IO]  = mock[GenGeoHash[IO]]
    val loader: GeoPointLoader[IO]           = mock[GeoPointLoader[IO]]
    val geoHashRegister: GeoHashRegister[IO] = mock[GeoHashRegister[IO]]
    val stub: ImportGeoPointsFromFile[IO]    = ImportGeoPointsFromFile.make(loader, geoHashRegister)
    val bufferedReader: BufferedReader       = mock[BufferedReader]
    val bufferResource                       = mkBufferResource(bufferedReader)

    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList("unknown,unknown")))

    assertThrows[NumberFormatException] {
      stub.importGeoPoints(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()
    }

    verify(loader).load(bufferedReader, batchSize, LazyList.empty[String])
  }
  it should "throw an conversion error when GeoPoint is invalid" in {

    implicit val genGeoHash: GenGeoHash[IO]  = mock[GenGeoHash[IO]]
    val loader: GeoPointLoader[IO]           = mock[GeoPointLoader[IO]]
    val geoHashRegister: GeoHashRegister[IO] = mock[GeoHashRegister[IO]]
    val stub: ImportGeoPointsFromFile[IO]    = ImportGeoPointsFromFile.make(loader, geoHashRegister)
    val bufferedReader: BufferedReader       = mock[BufferedReader]
    val bufferResource                       = mkBufferResource(bufferedReader)

    when(loader.load(bufferedReader, batchSize, LazyList.empty[String])).thenReturn(IO(LazyList("30000,30000")))

    assertThrows[GeoPointConversionError] {
      stub.importGeoPoints(bufferResource, batchSize, precision, onBatchFinish, onStart, onFinish).unsafeRunSync()
    }

  }
}
