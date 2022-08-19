package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.fixtures.CommandFixture
import com.stuart.geohash.infrastructure.ioc.provider.Commands
import com.stuart.geohash.infrastructure.stdio.Command.{CommandNotAvailable, InvalidArguments, ToolNotFound}
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper

class CommandFactorySpec extends UnitSpec with CommandFixture {

  "get command" should "return a import command when is present in the arguments" in {
    val command: Commands[IO]            = mock[Commands[IO]]
    val commandFactory                   = CommandFactory.make(command)
    val importCommand: ImportCommand[IO] = mock[ImportCommand[IO]]
    val helpCommand: HelpCommand[IO]     = mock[HelpCommand[IO]]

    when(command.importCommand).thenReturn(importCommand)
    when(command.helpCommand).thenReturn(helpCommand)

    val result = commandFactory.getCommand(argsWithImportCommand).unsafeRunSync()
    result shouldBe a[ImportCommand[IO]]
  }
  it should "return a help command when is present in the arguments" in {
    val command: Commands[IO]            = mock[Commands[IO]]
    val commandFactory                   = CommandFactory.make(command)
    val importCommand: ImportCommand[IO] = mock[ImportCommand[IO]]
    val helpCommand: HelpCommand[IO]     = mock[HelpCommand[IO]]

    when(command.importCommand).thenReturn(importCommand)
    when(command.helpCommand).thenReturn(helpCommand)

    val result = commandFactory.getCommand(argsWithHelpCommand).unsafeRunSync()
    result shouldBe a[HelpCommand[IO]]
  }
  it should "raise and error when the args dont have enough count to process a command" in {
    val command: Commands[IO]                                = mock[Commands[IO]]
    val commandFactory                                       = CommandFactory.make(command)
    val importCommand: ImportCommand[IO]                     = mock[ImportCommand[IO]]
    val helpCommand: HelpCommand[IO]                         = mock[HelpCommand[IO]]
    val commandLineRunnerHelper: CommandLineRunnerHelper[IO] = mock[CommandLineRunnerHelper[IO]]

    when(command.importCommand).thenReturn(importCommand)
    when(command.helpCommand).thenReturn(helpCommand)
    when(command.runnerHelper).thenReturn(commandLineRunnerHelper)
    when(commandLineRunnerHelper.printHelp).thenReturn(IO.unit)

    assertThrows[InvalidArguments] {
      commandFactory.getCommand("unknown".toArrayBySpace).unsafeRunSync()
    }
  }

  it should "raise and error when the tool name was not found" in {
    val command: Commands[IO]            = mock[Commands[IO]]
    val commandFactory                   = CommandFactory.make(command)
    val importCommand: ImportCommand[IO] = mock[ImportCommand[IO]]
    val helpCommand: HelpCommand[IO]     = mock[HelpCommand[IO]]

    when(command.importCommand).thenReturn(importCommand)
    when(command.helpCommand).thenReturn(helpCommand)

    assertThrows[ToolNotFound] {
      commandFactory.getCommand("unknown unknown".toArrayBySpace).unsafeRunSync()
    }
  }

  it should "raise and error when the command name was not found" in {
    val command: Commands[IO]            = mock[Commands[IO]]
    val commandFactory                   = CommandFactory.make(command)
    val importCommand: ImportCommand[IO] = mock[ImportCommand[IO]]
    val helpCommand: HelpCommand[IO]     = mock[HelpCommand[IO]]

    when(command.importCommand).thenReturn(importCommand)
    when(command.helpCommand).thenReturn(helpCommand)

    assertThrows[CommandNotAvailable] {
      commandFactory.getCommand("geohashcli unknown".toArrayBySpace).unsafeRunSync()
    }
  }
}
