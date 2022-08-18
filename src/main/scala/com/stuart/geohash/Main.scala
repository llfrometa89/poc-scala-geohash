package com.stuart.geohash

import cats.effect.std.Console
import cats.effect.{IO, IOApp, Sync}
import cats.implicits._
import com.stuart.geohash.infrastructure.ioc.provider.{Commands, Services}
import com.stuart.geohash.infrastructure.stdio.banner.Banner
import com.stuart.geohash.infrastructure.stdio.commands.CommandFactory
import com.stuart.geohash.cross.implicits._

object Main extends IOApp.Simple {

  override def run: IO[Unit] = program[IO]

  def program[F[_]: Console: Sync]: F[Unit] =
    for {
      _    <- Console[F].println(Banner.mkString("\n"))
      _    <- Console[F].println("Welcome to Stuart GeoHash CLI tool:")
      args <- Console[F].readLine
      services   = Services.make[F]()
      commands   = Commands.make(services.importGeoHash)
      factory    = CommandFactory.make(commands)
      argAsArray = args.toArrayBySpace
      command <- factory.getCommand(argAsArray)
      _       <- command.run(argAsArray).handleErrorWith(error => Console[F].errorln(error.getMessage))
    } yield ()

}
