package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import com.stuart.geohash.UnitSpec
import com.stuart.geohash.application.dto.geohash.GeoHashDTO
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.fixtures.{CommandFixture, GeoHashFixture}
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput
import com.stuart.geohash.infrastructure.stdio.{CommandOptions, CommandOptionsKeyword}
import org.apache.commons.cli._

import java.io.BufferedReader

class ImportCommandSpec extends UnitSpec with GeoHashFixture with CommandFixture {

  "run" should "execute import all GeoPoint when the import process was success" in {
    val commandOptions: CommandOptions[IO]                   = mock[CommandOptions[IO]]
    val importGeoHash: ImportGeoHash[IO]                     = mock[ImportGeoHash[IO]]
    val consoleOutput: ImportCommandFormatConsoleOutput[IO]  = mock[ImportCommandFormatConsoleOutput[IO]]
    val commandLineRunnerHelper: CommandLineRunnerHelper[IO] = mock[CommandLineRunnerHelper[IO]]
    val fakeParser: CommandLineParser                        = mock[DefaultParser]
    val fakeOptions: Options                                 = mock[Options]
    val fakeCommandLine: CommandLine                         = mock[CommandLine]

    val command = ImportCommand.make(commandOptions, importGeoHash, consoleOutput, commandLineRunnerHelper)

    when(commandLineRunnerHelper.parser).thenReturn(IO(fakeParser))
    when(commandOptions.getOptions).thenReturn(IO(fakeOptions))
    when(fakeParser.parse(fakeOptions, args)).thenReturn(fakeCommandLine)
    when(fakeCommandLine.hasOption(CommandOptionsKeyword.file)).thenReturn(true)
    when(fakeCommandLine.getOptionValue(CommandOptionsKeyword.file)).thenReturn("test_points.txt")
    when(fakeCommandLine.getOptionValue(CommandOptionsKeyword.batch)).thenReturn("500")
    when(fakeCommandLine.getOptionValue(CommandOptionsKeyword.precision)).thenReturn("5")
    when(fakeCommandLine.getOptionValue(CommandOptionsKeyword.format)).thenReturn("csv")
    when(
      importGeoHash.importGeoHash(
        any[Resource[IO, BufferedReader]],
        any[Int],
        any[Int],
        any[List[GeoHashDTO] => IO[Unit]],
        any[IO[Unit]],
        any[IO[Unit]]
      )
    )
      .thenReturn(IO.unit)

    command.run(args).unsafeRunSync()

    verify(commandLineRunnerHelper).parser
    verify(commandOptions).getOptions
    verify(fakeParser).parse(fakeOptions, args)
    verify(fakeCommandLine).hasOption(CommandOptionsKeyword.file)
    verify(fakeCommandLine).getOptionValue(CommandOptionsKeyword.file)
    verify(fakeCommandLine).getOptionValue(CommandOptionsKeyword.batch)
    verify(fakeCommandLine).getOptionValue(CommandOptionsKeyword.precision)
    verify(fakeCommandLine).getOptionValue(CommandOptionsKeyword.format)
    verify(importGeoHash).importGeoHash(
      any[Resource[IO, BufferedReader]],
      any[Int],
      any[Int],
      any[List[GeoHashDTO] => IO[Unit]],
      any[IO[Unit]],
      any[IO[Unit]]
    )
  }
  it should "catch and error when the import process was fail" in {
    val commandOptions: CommandOptions[IO]                   = mock[CommandOptions[IO]]
    val importGeoHash: ImportGeoHash[IO]                     = mock[ImportGeoHash[IO]]
    val consoleOutput: ImportCommandFormatConsoleOutput[IO]  = mock[ImportCommandFormatConsoleOutput[IO]]
    val commandLineRunnerHelper: CommandLineRunnerHelper[IO] = mock[CommandLineRunnerHelper[IO]]
    val fakeParser: CommandLineParser                        = mock[DefaultParser]
    val fakeOptions: Options                                 = mock[Options]

    val command = ImportCommand.make(commandOptions, importGeoHash, consoleOutput, commandLineRunnerHelper)

    when(commandLineRunnerHelper.parser).thenReturn(IO(fakeParser))
    when(commandOptions.getOptions).thenReturn(IO(fakeOptions))
    when(fakeParser.parse(fakeOptions, args)).thenThrow(new ParseException("error"))

    assertThrows[ParseException] {
      command.run(args).unsafeRunSync()
    }

    verify(commandLineRunnerHelper).parser
    verify(commandOptions).getOptions
    verify(fakeParser).parse(fakeOptions, args)
  }
}
