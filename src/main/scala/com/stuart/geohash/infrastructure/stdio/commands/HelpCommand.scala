package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.Sync
import cats.effect.std.Console
import com.stuart.geohash.infrastructure.stdio.CommandLineRunner
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper

trait HelpCommand[F[_]] extends CommandLineRunner[F] {

  def run(args: Array[String]): F[Unit]
}

object HelpCommand {

  def make[F[_]: Sync: Console](commandLineRunnerHelper: CommandLineRunnerHelper[F]): HelpCommand[F] =
    new HelpCommand[F] {
      def run(args: Array[String]): F[Unit] = commandLineRunnerHelper.printHelp
    }
}
