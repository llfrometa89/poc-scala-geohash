package com.stuart.geohash.infrastructure.stdio.commands

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.infrastructure.ioc.provider.Commands
import com.stuart.geohash.infrastructure.stdio.Command.{CommandNotAvailable, InvalidArguments, ToolNotFound}
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.{CommandLineRunner, CommandOptions, CommandOptionsKeyword => Keyword}

trait CommandFactory[F[_]] {
  def getCommand(args: Array[String]): F[CommandLineRunner[F]]
}

object CommandFactory {

  def make[F[_]: Sync](command: Commands[F]): CommandFactory[F] =
    new CommandFactory[F] {

      val availableTools    = List("geohashcli")
      val availableCommands = Map(Keyword.`import` -> command.importCommand)

      def getCommand(args: Array[String]): F[CommandLineRunner[F]] = for {
        _              <- Sync[F].whenA(args.size < 2)(printHelp *> Sync[F].raiseError(InvalidArguments(args)))
        tool           <- Sync[F].delay(args(0))
        commandKeyword <- Sync[F].delay(args(1))
        _              <- Sync[F].whenA(!availableTools.contains(tool))(Sync[F].raiseError(ToolNotFound(tool)))
        command        <- availableCommands.get(commandKeyword).liftTo[F](CommandNotAvailable(commandKeyword))
      } yield command

      private def printHelp =
        command.runnerHelper.printHelp
    }
}
