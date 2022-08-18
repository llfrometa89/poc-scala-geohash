package com.stuart.geohash.cross.implicits.syntax

import com.stuart.geohash.UnitSpec

import com.stuart.geohash.cross.implicits._

class StringSyntaxSpec extends UnitSpec {

  "convert String to Array split by space" should "return an array split by space with three items" in {
    val aValue = "a b c"
    val result = aValue.toArrayBySpace.toList
    result should have size 3
    result.head shouldBe "a"
    result(1) shouldBe "b"
    result.last shouldBe "c"

  }
  it should "return an array split by space with one item when the string dont have spaces" in {
    val aValue = "abc"
    val result = aValue.toArrayBySpace.toList
    result should have size 1
    result.head shouldBe "abc"
  }

}
