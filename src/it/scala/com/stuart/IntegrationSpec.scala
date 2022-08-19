package com.stuart

import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class IntegrationSpec extends AnyFlatSpec with BeforeAndAfter with BeforeAndAfterAll with MockitoSugar with Matchers {}
