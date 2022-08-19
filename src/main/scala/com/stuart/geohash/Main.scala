package com.stuart.geohash

import cats.Parallel
import cats.effect.std.Console
import cats.effect.{Async, IO, IOApp, Sync}
import cats.implicits._
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.infrastructure.configuration.Config
import com.stuart.geohash.infrastructure.db.liquibase.LiquibaseFactory
import com.stuart.geohash.infrastructure.ioc.provider.{AppResources, Commands, Repositories, Services}
import com.stuart.geohash.infrastructure.stdio.banner.Banner
import com.stuart.geohash.infrastructure.stdio.commands.CommandFactory

object Main extends IOApp.Simple {

  override def run: IO[Unit] = Config.load[IO].flatMap { config =>
    val resources = AppResources.make[IO](config)
    initialize(resources) >> program(resources)
  }

  def initialize[F[_]: Sync](resources: AppResources[F]): F[Unit] = {
    val liquibase = LiquibaseFactory.make[F](resources.mysql)
    liquibase.update()
  }

  def program[F[_]: Async: Parallel: Console](resources: AppResources[F]): F[Unit] =
    for {
      _    <- Console[F].println(Banner.mkString("\n"))
      _    <- Console[F].println("Welcome to Stuart GeoHash CLI tool:")
      args <- Console[F].readLine
      repositories = Repositories.make[F](resources.mysql)
      services     = Services.make[F](repositories)
      commands     = Commands.make(services.importGeoHash)
      factory      = CommandFactory.make(commands)
      argAsArray   = args.toArrayBySpace
      command <- factory.getCommand(argAsArray)
      _       <- command.run(argAsArray).handleErrorWith(error => Console[F].errorln(error.getMessage))
    } yield ()

}
