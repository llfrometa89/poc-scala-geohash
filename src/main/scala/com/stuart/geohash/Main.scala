package com.stuart.geohash

import cats.effect.{IO, IOApp}
import cats.implicits._
import ch.hsr.geohash.GeoHash

object Main extends IOApp.Simple {

  override def run: IO[Unit] = for {
    a <- IO.delay(GeoHash.fromGeohashString("sp3e3"))
    _ <- IO(
      println(s"Hello from GeoHash: ${a.getOriginatingPoint.getLatitude}")
    ) >> IO(
      println(s"Hello GeoHash: ${GeoHash.geoHashStringWithCharacterPrecision(41.388828145321, 2.1689976634898, 12)}")
    ) >>
      IO(
        println(s"Hello GeoHash: ${GeoHash.geoHashStringWithCharacterPrecision(41.388828145321, 2.1689976634898, 5)}")
      ) >> IO(
        println(s"Hello GeoHash: ${GeoHash.geoHashStringWithCharacterPrecision(41.390743, 2.138177, 8)}")
      ) >> IO(
        println(s"Hello GeoHash: ${GeoHash.geoHashStringWithCharacterPrecision(41.390853, 2.138067, 8)}")
      )
  } yield ()
}
