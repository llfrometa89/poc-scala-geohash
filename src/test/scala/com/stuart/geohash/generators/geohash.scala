package com.stuart.geohash.generators

import com.stuart.geohash.domain.model.geohash.{Latitude, Longitude, Point}
import org.scalacheck.{Arbitrary, Gen}

object geohash {

  val precisionGen: Gen[Int] = for {
    precision <- Gen.chooseNum(5, 12)
  } yield precision

  val coordinateGen: Gen[Double] = for {
    coordinate <- Gen.chooseNum(-90.0d, 90.0d)
  } yield coordinate

  val pointGen: Gen[Point] = for {
    latitude  <- coordinateGen
    longitude <- coordinateGen
  } yield Point(Latitude(latitude), Longitude(longitude))

  val geohashAsStringGen: Gen[String] = Gen.nonEmptyListOf[Char](Arbitrary.arbChar.arbitrary).map(_.mkString)

}
