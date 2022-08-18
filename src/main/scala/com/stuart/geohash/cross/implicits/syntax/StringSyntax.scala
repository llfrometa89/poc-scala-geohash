package com.stuart.geohash.cross.implicits.syntax

trait StringSyntax {

  implicit class ImplicitStringSyntax[T](s: String) {
    def toArrayBySpace: Array[String] = s.split(" ")
    def toArrayByComma: Array[String] = s.split(",")
  }

  object String {
    val empty: String = ""
  }
}
