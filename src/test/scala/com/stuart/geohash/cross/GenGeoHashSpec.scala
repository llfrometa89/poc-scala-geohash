package com.stuart.geohash.cross

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.domain.models.geohash.GeoPoint
import com.stuart.geohash.fixtures.ErrorFixture
import com.stuart.geohash.generators.geohash._

class GenGeoHashSpec extends UnitSpec with ErrorFixture {

  "make a GeoPoint" should "return a GeoHash as string" in {
    forAll(pointGen, precisionGen, geohashAsStringGen) { case (point, precision, geohashAsString) =>
      val geoHashGenerator = dataMakeGeoHash(geohashAsString)
      val result           = geoHashGenerator.make(point, precision).unsafeRunSync()
      result shouldBe geohashAsString
    }
  }
  it should "throw an error when the creation fail" in {
    forAll(pointGen, precisionGen, geohashAsStringGen) { case (point, precision, geohashAsString) =>
      val geoHashGenerator = failingMakeGeoHash(geohashAsString)
      assertThrows[DummyError] {
        geoHashGenerator.make(point, precision).unsafeRunSync()
      }
    }
  }

  def dataMakeGeoHash(geohashAsString: String): TestGenGeoHash = new TestGenGeoHash {
    override def make(point: GeoPoint, precision: Int): IO[String] =
      IO.pure(geohashAsString)
  }

  def failingMakeGeoHash(geohashAsString: String): TestGenGeoHash = new TestGenGeoHash {
    override def make(point: GeoPoint, precision: Int): IO[String] =
      IO.raiseError(DummyError("error")) *> IO.pure(geohashAsString)
  }
}

protected class TestGenGeoHash extends GenGeoHash[IO] {
  def make(point: GeoPoint, precision: Int): IO[String] = IO.pure(String.empty)
}
