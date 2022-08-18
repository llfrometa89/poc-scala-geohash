package com.stuart.geohash.infrastructure.stdio

import cats.effect.Sync
import cats.implicits._
import org.apache.commons.cli.{CommandLineParser, DefaultParser, HelpFormatter}

trait CommandLineRunner[F[_]] {
  def run(args: Array[String]): F[Unit]
}

object Command {
  abstract class CommandError(val message: String) extends Exception(message)
  case class ToolNotFound(tool: String)            extends CommandError(s"The tool ($tool) was not found")
  case class InvalidArguments(args: Array[String]) extends CommandError(s"Invalid arguments (${args.mkString(" ")})")
  case class CommandNotAvailable(command: String)  extends CommandError(s"The command ($command) is not available")
}

object CommandLineRunnerHelper {

  lazy val defaultParser: CommandLineParser = new DefaultParser

  val formatter: HelpFormatter = new HelpFormatter
  val usageLabel: String       = "geohashcli [command] [options] [target [target2 [target3] ...]]"

  def parser[F[_]: Sync](): F[CommandLineParser] = Sync[F].delay(defaultParser)

  def printHelp[F[_]: Sync](commandOptions: CommandOptions[F]): F[Unit] =
    for {
      options <- commandOptions.getOptions
      _       <- Sync[F].delay(formatter.printHelp(usageLabel, options))
    } yield ()
}
