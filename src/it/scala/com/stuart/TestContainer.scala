package com.stuart

import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File

trait TestContainer {

  def initializeTestContainer(): Unit = startLocalInfrastructure()

  private def startLocalInfrastructure(): Unit = {
    val configuration = new File("src/it/resources/docker-infrastructure.yml")

    val container: DockerComposeContainer[_] = new DockerComposeContainer(configuration)
      .waitingFor("mysql", Wait.forHealthcheck())

    container.start()
  }

}
