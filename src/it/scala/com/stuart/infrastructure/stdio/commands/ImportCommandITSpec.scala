package com.stuart.infrastructure.stdio.commands

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.stuart.IntegrationSpec
import com.stuart.fixtures.GeoHashFixtureIT
import com.stuart.geohash.application.dto.geohash.{GeoPointConversionError, InvalidArrayConversionError}
import com.stuart.geohash.application.services.ImportGeoHash
import com.stuart.geohash.cross.implicits._
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.domain.services.GeoHashRegister
import com.stuart.geohash.infrastructure.repositories.{GeoHashMySqlRepository, GeoHashSQL}
import com.stuart.geohash.infrastructure.services.GeoPointLoader
import com.stuart.geohash.infrastructure.stdio.Command.InvalidArguments
import com.stuart.geohash.infrastructure.stdio.CommandOptions
import com.stuart.geohash.infrastructure.stdio.commands.ImportCommand
import com.stuart.geohash.infrastructure.stdio.helpers.CommandLineRunnerHelper
import com.stuart.geohash.infrastructure.stdio.output.ImportCommand.ImportCommandFormatConsoleOutput

class ImportCommandITSpec extends IntegrationSpec with GeoHashFixtureIT {

  before {
    cleanDatabase()
  }

  "run" should "import all GeoPoint when the import process was success" in {
    val args =
      "geohashcli import --file=src/it/resources/geopoints/test_points_valid_geopoints.txt --precision=5 --batch=500 --format=csv"

    val computation = for {
      command   <- getCommandInstance
      _         <- command.run(args.toArrayBySpace)
      repo      <- getRepository
      geoHashes <- repo.findAll(0, 5)
    } yield geoHashes

    val result = computation.unsafeRunSync()

    result should have size 3
  }
  it should "throw an error when the command argument is invalid" in {
    val args =
      "geohashcli import --file=src/it/resources/geopoints/test_points_invalid_structure_geopoints.txt --precision=5 --batch=500 --format=csv"

    val computation = for {
      command <- getCommandInstance
      _       <- command.run(args.toArrayBySpace)
    } yield ()

    assertThrows[InvalidArrayConversionError.type] {
      computation.unsafeRunSync()
    }

  }
  it should "throw an error when found invalid geopoints" in {
    val args =
      "geohashcli import --file=src/it/resources/geopoints/test_points_invalid_geopoints.txt --precision=5 --batch=500 --format=csv"

    val computation = for {
      command <- getCommandInstance
      _       <- command.run(args.toArrayBySpace)
    } yield ()

    assertThrows[GeoPointConversionError] {
      computation.unsafeRunSync()
    }

  }

  def getCommandInstance: IO[ImportCommand[IO]] = for {
    geoHashRepository       <- getRepository
    loader                  <- IO.pure(GeoPointLoader.make[IO]())
    geoHashRegister         <- IO.pure(GeoHashRegister.make(geoHashRepository))
    commandOptions          <- IO.pure(CommandOptions.make[IO]())
    importGeoHash           <- IO.pure(ImportGeoHash.make[IO](loader, geoHashRegister))
    consoleOutput           <- IO.pure(ImportCommandFormatConsoleOutput.make[IO])
    commandLineRunnerHelper <- IO.pure(CommandLineRunnerHelper.make[IO](commandOptions))
    command <- IO.pure(ImportCommand.make[IO](commandOptions, importGeoHash, consoleOutput, commandLineRunnerHelper))
  } yield command

  def getRepository: IO[GeoHashRepository[IO]] = for {
    mySqlClient <- mySqlClientTest
    repository  <- IO.delay(GeoHashMySqlRepository.make[IO](mySqlClient, GeoHashSQL.make))
  } yield repository

  def cleanDatabase(): Unit = getRepository.flatMap(_.deleteAll()).unsafeRunSync()
}
