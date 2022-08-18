package com.stuart.geohash.infrastructure.stdio

import cats.effect.Sync
import cats.implicits._
import org.apache.commons.cli.Options
import com.stuart.geohash.infrastructure.stdio.{CommandOptionsKeyword => Keyword}

trait CommandOptions[F[_]] {
  def getOptions: F[Options]
}

object CommandOptions {

  def make[F[_]: Sync](): CommandOptions[F] = new CommandOptions[F] {

    def mkCreateCmdOptions: F[Options] = Sync[F].delay(new Options())

    def getOptions: F[Options] = for {
      options <- mkCreateCmdOptions
      _ <- Sync[F].delay(options.addOption(Keyword.`import`, Keyword.`import`, false, "Allow to set file address"))
      _ <- Sync[F].delay(options.addOption("f", Keyword.file, true, "Allow to set file address"))
      _ <- Sync[F].delay(options.addOption("fmt", Keyword.format, true, "Allow to set file format"))
      _ <- Sync[F].delay(options.addOption("b", Keyword.batch, true, "Allow to set batch value"))
      _ <- Sync[F].delay(options.addOption("p", Keyword.precision, true, "Allow to set geohash precision"))
    } yield options
  }

}

object CommandOptionsKeyword {
  val `import`  = "import"
  val file      = "file"
  val format    = "format"
  val batch     = "batch"
  val precision = "precision"
}
