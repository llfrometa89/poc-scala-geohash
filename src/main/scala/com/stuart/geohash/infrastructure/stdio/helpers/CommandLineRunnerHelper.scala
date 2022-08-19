package com.stuart.geohash.infrastructure.stdio.helpers

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.infrastructure.stdio.CommandOptions
import org.apache.commons.cli.{CommandLineParser, DefaultParser, HelpFormatter}

trait CommandLineRunnerHelper[F[_]] {
  def parser: F[CommandLineParser]
  def printHelp: F[Unit]
}

object CommandLineRunnerHelper {

  def make[F[_]: Sync](commandOptions: CommandOptions[F]): CommandLineRunnerHelper[F] = new CommandLineRunnerHelper[F] {
    lazy val defaultParser: CommandLineParser = new DefaultParser

    val formatter: HelpFormatter = new HelpFormatter
    val usageLabel: String       = "geohashcli [command] [options] [target [target2 [target3] ...]]"

    def parser: F[CommandLineParser] = Sync[F].delay(defaultParser)

    def printHelp: F[Unit] =
      for {
        options <- commandOptions.getOptions
        _       <- Sync[F].delay(formatter.printHelp(usageLabel, options))
      } yield ()
  }

}
