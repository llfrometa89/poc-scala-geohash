package com.stuart.geohash

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UnitSpec
    extends AnyFlatSpec
    with BeforeAndAfter
    with Matchers
    with MockitoSugar
    with ArgumentMatchersSugar
    with ScalaCheckDrivenPropertyChecks
