package com.stuart.geohash.cross.implicits.syntax

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.concurrent.duration._

trait FiniteDurationSyntax {

  implicit class ImplicitFiniteDurationSyntax(d: FiniteDuration) {

    def formatDateTime: String = {
      val date = Instant.ofEpochMilli(d.toMillis).atZone(ZoneId.systemDefault()).toLocalDateTime;
      DateTimeFormatter.ISO_DATE_TIME.format(date)
    }
  }

  implicit class ImplicitTupleFiniteDurationSyntax(t: (FiniteDuration, FiniteDuration)) {
    def elapsedTime: String = {
      val (start, end) = t
      val et           = end - start
      val min          = et.toMinutes
      val formatMin    = (m: Long) => s"${m}min"
      formatMin(min)
    }
  }
}
