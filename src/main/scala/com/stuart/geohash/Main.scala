package com.stuart.geohash

import cats.effect.std.Console
import cats.effect.{IO, IOApp, Sync}
import cats.implicits._
import com.stuart.geohash.infrastructure.ioc.provider.{Commands, Services}
import com.stuart.geohash.infrastructure.stdio.banner.Banner

object Main extends IOApp.Simple {

  override def run: IO[Unit] = program[IO]

  def program[F[_]: Console: Sync]: F[Unit] =
    for {
      _    <- Console[F].println(Banner.mkString("\n"))
      _    <- Console[F].println("Welcome to Stuart GeoHash CLI tool:")
      args <- Console[F].readLine
      services = Services.make[F]()
      commands = Commands.make(services.importGeoHash)
      command  = commands.importCommand
      _ <- command.run(toArgsArray(args))
    } yield ()

  private def toArgsArray(args: String): Array[String] = args.split(" ")
}
