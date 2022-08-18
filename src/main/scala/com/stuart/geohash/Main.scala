package com.stuart.geohash

import cats.effect.std.Console
import cats.effect.{Async, IO, IOApp}
import cats.implicits._
import com.stuart.geohash.infrastructure.ioc.provider.{AppResources, Commands, Repositories, Services}
import com.stuart.geohash.infrastructure.stdio.banner.Banner
import com.stuart.geohash.infrastructure.stdio.commands.CommandFactory
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.infrastructure.configuration.Config

object Main extends IOApp.Simple {

  override def run: IO[Unit] = Config.load[IO].flatMap { config =>
    val resources = AppResources.make[IO](config)
    program(resources)
  }

  def program[F[_]: Async: Console](resources: AppResources[F]): F[Unit] =
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
