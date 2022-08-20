package com.stuart

import cats.effect.IO
import com.stuart.geohash.infrastructure.db.liquibase.LiquibaseFactory
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.DockerComposeContainer.ComposeFile
import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, WaitingForService}
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

class IntegrationSpec
    extends AnyFlatSpec
    with BeforeAndAfter
    with BeforeAndAfterAll
    with MockitoSugar
    with Matchers
    with MySqlTestingConnection
    with TestContainerForAll {

  override val containerDef: ContainerDef =
    DockerComposeContainer
      .Def(
        ComposeFile(Left(new File("src/it/resources/docker-compose.yml"))),
        waitingFor = Option(WaitingForService("mysql", Wait.forHealthcheck()))
      )

  override def afterContainersStart(container: Containers): Unit = {
    super.afterContainersStart(container)
    container match {
      case _: DockerComposeContainer => initializeDatabase()
    }
  }

  def initializeDatabase(): Unit = {

    val initializer = for {
      mySqlClient <- mySqlClientTest
      liquibase   <- IO.delay(LiquibaseFactory.make[IO](mySqlClient))
      _           <- liquibase.update()
    } yield ()

    initializer.unsafeRunSync()
  }

}
