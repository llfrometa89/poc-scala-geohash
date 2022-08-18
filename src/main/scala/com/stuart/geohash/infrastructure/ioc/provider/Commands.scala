package com.stuart.geohash.infrastructure.ioc.provider

import cats.effect.Sync
import cats.effect.std.Console
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.infrastructure.stdio.CommandOptions
import com.stuart.geohash.infrastructure.stdio.commands.ImportCommand
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput

sealed abstract class Commands[F[_]] private (
  val importCommand: ImportCommand[F]
)

object Commands {
  def make[F[_]: Sync: Console](importGeoHash: ImportGeoHash[F]): Commands[F] = {

    val options = CommandOptions.make[F]()
    new Commands[F](
      importCommand = ImportCommand
        .make[F](
          commandOptions = options,
          importGeoHash = importGeoHash,
          consoleOutput = ImportCommandFormatConsoleOutput.make[F]
        )
    ) {}
  }
}
