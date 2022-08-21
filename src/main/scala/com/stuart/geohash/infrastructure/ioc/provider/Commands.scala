package com.stuart.geohash.infrastructure.ioc.provider

import cats.effect.Sync
import cats.effect.std.Console
import com.stuart.geohash.application.services.ImportGeoPointsFromFile
import com.stuart.geohash.infrastructure.stdio.CommandOptions
import com.stuart.geohash.infrastructure.stdio.commands.{HelpCommand, ImportCommand}
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput

sealed abstract class Commands[F[_]] private (
  val importCommand: ImportCommand[F],
  val helpCommand: HelpCommand[F],
  val runnerHelper: CommandLineRunnerHelper[F]
)

object Commands {
  def make[F[_]: Sync: Console](importGeoPoints: ImportGeoPointsFromFile[F]): Commands[F] = {

    val options      = CommandOptions.make[F]()
    val runnerHelper = CommandLineRunnerHelper.make[F](options)
    new Commands[F](
      importCommand = ImportCommand
        .make[F](
          commandOptions = options,
          importGeoPoints = importGeoPoints,
          consoleOutput = ImportCommandFormatConsoleOutput.make[F],
          runnerHelper
        ),
      helpCommand = HelpCommand.make[F](runnerHelper),
      runnerHelper = runnerHelper
    ) {}
  }
}
