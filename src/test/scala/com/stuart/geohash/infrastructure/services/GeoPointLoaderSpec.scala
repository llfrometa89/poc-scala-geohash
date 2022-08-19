package com.stuart.geohash.infrastructure.services

import cats.effect.IO
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.fixtures.GeoHashFixture
import cats.effect.unsafe.implicits.global

import java.io.BufferedReader

class GeoPointLoaderSpec extends UnitSpec with GeoHashFixture {

  "lazy load" should "return GeoPoint list from buffer" in {
    val loader         = GeoPointLoader.make[IO]()
    val bufferedReader = mock[BufferedReader]
    val batchSize: Int = 2
    val line1          = "41.388828145321,2.1689976634898"
    val line2          = "41.390743,2.1647467"

    when(bufferedReader.readLine()).thenReturn(line1, line2, null)

    val result = loader.load(bufferedReader, batchSize, LazyList.empty[String]).unsafeRunSync()

    result.toList should have size 2
    result.toList shouldBe List(line1, line2)
  }
}
