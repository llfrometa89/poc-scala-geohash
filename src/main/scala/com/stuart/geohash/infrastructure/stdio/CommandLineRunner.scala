package com.stuart.geohash.infrastructure.stdio

trait CommandLineRunner[F[_]] {
  def run(args: Array[String]): F[Unit]
}

object Command {
  abstract class CommandError(val message: String) extends Exception(message)
  case class ToolNotFound(tool: String)            extends CommandError(s"The tool ($tool) was not found")
  case class InvalidArguments(args: Array[String]) extends CommandError(s"Invalid arguments (${args.mkString(" ")})")
  case class CommandNotAvailable(command: String)  extends CommandError(s"The command ($command) is not available")
}
