package com.stuart.geohash

import cats.effect.std.Console
import cats.effect.{IO, IOApp, Sync}
import cats.implicits._
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.infrastructure.stdio.CommandOptions
import com.stuart.geohash.infrastructure.stdio.banner.Banner
import com.stuart.geohash.infrastructure.stdio.commands.ImportCommand

object Main extends IOApp.Simple {

  override def run: IO[Unit] = program[IO]

  def program[F[_]: Console: Sync]: F[Unit] =
    for {
      _       <- Console[F].println(Banner.mkString("\n"))
      _       <- Console[F].println("Welcome to Stuart GeoHash CLI tool:")
      args    <- Console[F].readLine
      command <- Sync[F].delay(ImportCommand.make[F](commandOptions = CommandOptions.make[F](), ImportGeoHash.make[F]))
      _       <- command.run(toArgsArray(args))
    } yield ()

  private def toArgsArray(args: String): Array[String] = args.split(" ")
}
