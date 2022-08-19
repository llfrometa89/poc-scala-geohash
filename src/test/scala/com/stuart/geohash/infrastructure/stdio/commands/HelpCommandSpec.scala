package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.fixtures.CommandFixture
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper

class HelpCommandSpec extends UnitSpec with CommandFixture {

  "run" should "show the help description with available commands and options" in {

    val commandLineRunnerHelper: CommandLineRunnerHelper[IO] = mock[CommandLineRunnerHelper[IO]]
    val command                                              = HelpCommand.make(commandLineRunnerHelper)

    when(commandLineRunnerHelper.printHelp).thenReturn(IO.unit)

    command.run(args).unsafeRunSync()

    verify(commandLineRunnerHelper).printHelp
  }
}
