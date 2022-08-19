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
      _       <- Sync[F].delay(options.addOption(Keyword.`import`, Keyword.`import`, false, "Execute import command"))
      _       <- Sync[F].delay(options.addOption(Keyword.`help`, Keyword.`help`, false, "Execute help command"))
      _       <- Sync[F].delay(options.addOption("f", Keyword.file, true, "Allow to set file address"))
      _       <- Sync[F].delay(options.addOption("fmt", Keyword.format, true, "Allow to set file format"))
      _       <- Sync[F].delay(options.addOption("b", Keyword.batch, true, "Allow to set batch value"))
      _       <- Sync[F].delay(options.addOption("p", Keyword.precision, true, "Allow to set geohash precision"))
    } yield options
  }

}

object CommandOptionsKeyword {

  type Command = String
  type Options = String

  val `import`: Command  = "import"
  val help: Command      = "help"
  val helpAlias: Command = "--help"
  val file: Options      = "file"
  val format: Options    = "format"
  val batch: Options     = "batch"
  val precision: Options = "precision"
}
