package com.stuart.geohash.fixtures

trait ErrorFixture {
  case class DummyError(message: String) extends Exception(message)
}
