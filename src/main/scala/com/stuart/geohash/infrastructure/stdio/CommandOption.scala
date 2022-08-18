package com.stuart.geohash.infrastructure.stdio

import cats.effect.Sync
import cats.implicits._
import org.apache.commons.cli.Options

trait CommandOptions[F[_]] {
  def getOptions: F[Options]
}

object CommandOptions {

  def make[F[_]: Sync](): CommandOptions[F] = new CommandOptions[F] {

    def mkCreateCmdOptions: F[Options] = Sync[F].delay(new Options())

    def getOptions: F[Options] = for {
      options <- mkCreateCmdOptions
      _       <- Sync[F].delay(options.addOption("f", CommandOptionsKeyword.file, true, "Allow to set file address"))
      _       <- Sync[F].delay(options.addOption("fmt", CommandOptionsKeyword.format, true, "Allow to set file format"))
      _       <- Sync[F].delay(options.addOption("b", CommandOptionsKeyword.batch, true, "Allow to set batch value"))
    } yield options
  }

}

object CommandOptionsKeyword {
  val file   = "file"
  val format = "format"
  val batch  = "batch"
}
