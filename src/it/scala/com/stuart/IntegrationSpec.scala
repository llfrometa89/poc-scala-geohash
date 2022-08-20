package com.stuart

import cats.effect.IO
import com.stuart.geohash.infrastructure.db.liquibase.LiquibaseFactory
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import cats.effect.unsafe.implicits.global

class IntegrationSpec
    extends AnyFlatSpec
    with BeforeAndAfter
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers
    with TestContainer
    with MySqlTestingConnection {

  def initializeDatabase(): Unit = {

    val initializer = for {
      mySqlClient <- mySqlClientTest
      _           <- IO.delay(initializeTestContainer())
      liquibase   <- IO.delay(LiquibaseFactory.make[IO](mySqlClient))
      _           <- liquibase.update()
    } yield ()

    initializer.unsafeRunSync()
  }

}
