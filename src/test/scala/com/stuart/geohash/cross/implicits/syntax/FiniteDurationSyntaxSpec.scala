package com.stuart.geohash.cross.implicits.syntax

import cats.effect.IO
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.cross.implicits._
import cats.effect.unsafe.implicits.global

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._

class FiniteDurationSyntaxSpec extends UnitSpec {

  "formatDateTime" should "return a string date in the ISO-8601 standard" in {
    val duration: FiniteDuration = IO.realTime.unsafeRunSync()
    val date = Instant.ofEpochMilli(duration.toMillis).atZone(ZoneId.systemDefault()).toLocalDateTime;
    DateTimeFormatter.ISO_DATE_TIME.format(date)
    DateTimeFormatter.ISO_DATE_TIME.format(date) shouldBe duration.formatDateTime

  }

  "elapsedTime" should "return elapsed time in minutes when de time is less than a min" in {
    val start  = IO.realTime.unsafeRunSync()
    val end    = IO.realTime.unsafeRunSync() + 500.milliseconds
    val result = (start, end).elapsedTime
    result shouldBe "0min"
  }
  "elapsedTime" should "return elapsed time in minutes when de time is more than a min" in {
    val start  = IO.realTime.unsafeRunSync()
    val end    = IO.realTime.unsafeRunSync() + 2.minutes
    val result = (start, end).elapsedTime
    result shouldBe "2min"
  }

}
