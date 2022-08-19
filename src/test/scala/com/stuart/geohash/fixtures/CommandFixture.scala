package com.stuart.geohash.fixtures

import com.stuart.geohash.cross.implicits._

trait CommandFixture {

  lazy val args: Array[String] =
    "geohashcli import --file=test_points.txt --precision=5 --batch=500 --format=csv".toArrayBySpace
}
