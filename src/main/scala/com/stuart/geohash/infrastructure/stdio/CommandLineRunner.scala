package com.stuart.geohash.infrastructure.stdio

import cats.effect.Sync
import cats.implicits._
import org.apache.commons.cli.{CommandLineParser, DefaultParser, HelpFormatter}

trait CommandLineRunner[F[_]] {

  def run(args: Array[String]): F[Unit]

}

object CommandLineRunnerHelper {

  lazy val defaultParser: CommandLineParser = new DefaultParser

  val formatter: HelpFormatter = new HelpFormatter
  val usageLabel: String       = "geohash-cli [options] [target [target2 [target3] ...]]"

  def parser[F[_]: Sync](): F[CommandLineParser] = Sync[F].delay(defaultParser)

  def printHelp[F[_]: Sync](commandOptions: CommandOptions[F]): F[Unit] =
    for {
      options <- commandOptions.getOptions
      _       <- Sync[F].delay(formatter.printHelp(usageLabel, options))
    } yield ()
}
