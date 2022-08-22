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
      val mPrecision       = Some(precision)
      val geoHashGenerator = dataMakeGeoHash(geohashAsString)
      val result           = geoHashGenerator.make(point, mPrecision).unsafeRunSync()
      result shouldBe geohashAsString
    }
  }
  it should "throw an error when the creation fail" in {
    forAll(pointGen, precisionGen, geohashAsStringGen) { case (point, precision, geohashAsString) =>
      val mPrecision       = Some(precision)
      val geoHashGenerator = failingMakeGeoHash(geohashAsString)
      assertThrows[DummyError] {
        geoHashGenerator.make(point, mPrecision).unsafeRunSync()
      }
    }
  }
  it should "return a GeoHash as string with precision length when precision is None" in {
    val geoHashAsString  = "sp3e2wuy"
    val point            = GeoPoint.fromDouble(41.390743, 41.390743)
    val geoHashGenerator = dataMakeGeoHash(geoHashAsString)
    val result           = geoHashGenerator.make(point, None).unsafeRunSync()
    result shouldBe geoHashAsString
  }

  def dataMakeGeoHash(geoHashAsString: String): TestGenGeoHash = new TestGenGeoHash {
    override def make(point: GeoPoint, precision: Option[Int]): IO[String] =
      IO.pure(geoHashAsString)
  }

  def failingMakeGeoHash(geohashAsString: String): TestGenGeoHash = new TestGenGeoHash {
    override def make(point: GeoPoint, precision: Option[Int]): IO[String] =
      IO.raiseError(DummyError("error")) *> IO.pure(geohashAsString)
  }
}

protected class TestGenGeoHash extends GenGeoHash[IO] {
  def make(point: GeoPoint, precision: Option[Int]): IO[String] = IO.pure(String.empty)
}
