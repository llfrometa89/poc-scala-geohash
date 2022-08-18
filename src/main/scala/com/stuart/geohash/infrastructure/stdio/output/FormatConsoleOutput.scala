package com.stuart.geohash.infrastructure.stdio.output

abstract class FormatConsoleOutput(val repr: String)

object FormatConsoleOutput {
  final val csv  = "csv"
  final val json = "json"

  def apply(format: String): FormatConsoleOutput = format match {
    case FormatConsoleOutput.json => JsonFormatConsoleOutput
    case _                        => CsvFormatConsoleOutput
  }
}

case object CsvFormatConsoleOutput  extends FormatConsoleOutput(FormatConsoleOutput.csv)
case object JsonFormatConsoleOutput extends FormatConsoleOutput(FormatConsoleOutput.json)
