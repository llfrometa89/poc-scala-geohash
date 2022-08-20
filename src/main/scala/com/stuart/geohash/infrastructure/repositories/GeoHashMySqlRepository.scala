package com.stuart.geohash.infrastructure.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.stuart.geohash.domain.models.geohash.GeoHash.GeoHashExecutionError
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import com.stuart.geohash.infrastructure.repositories.GeoHashSQL.GeoHashEntity
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

import java.sql.SQLIntegrityConstraintViolationException

object GeoHashMySqlRepository {

  def make[F[_]: Async](mySqlClient: MySqlClient[F], geoHashSQL: GeoHashSQL[ConnectionIO]): GeoHashRepository[F] =
    new GeoHashRepository[F] {

      def create(geoHash: GeoHash): F[GeoHash] = {
        val computation = for {
          ax <- mySqlClient.transactor
          _  <- mkExecute(geoHashSQL.insert(geoHash), ax)
        } yield geoHash

        computation.handleErrorWith {
          case e: SQLIntegrityConstraintViolationException => Async[F].raiseError(GeoHashExecutionError(e.getMessage))
          case e                                           => Async[F].raiseError(e)
        }
      }

      def findBy(geoHashMaxPrecision: GeoHashMaxPrecision): F[Option[GeoHash]] =
        for {
          ax      <- mySqlClient.transactor
          mEntity <- mkExecute(geoHashSQL.selectByGeoHash(geoHashMaxPrecision), ax)
          geoHash <- mEntity.map(_.toGeoHash).pure[F]
        } yield geoHash

      def findAll(page: Int, size: Int): F[List[GeoHash]] =
        for {
          ax        <- mySqlClient.transactor
          entities  <- mkExecute(geoHashSQL.selectAll(page, size), ax)
          geoHashes <- entities.map(_.toGeoHash).pure[F]
        } yield geoHashes

      override def deleteAll(): F[Unit] =
        for {
          ax <- mySqlClient.transactor
          _  <- mkExecute(geoHashSQL.deleteAll(), ax)
        } yield ()

      private def mkExecute[A](connectionIO: ConnectionIO[A], transactor: Transactor[F]): F[A] =
        connectionIO.transact(transactor)
    }
}

trait GeoHashSQL[F[_]] {
  def insert(geoHash: GeoHash): F[Int]
  def selectAll(page: Int, size: Int): F[List[GeoHashEntity]]
  def selectByGeoHash(geoHash: GeoHashMaxPrecision): F[Option[GeoHashEntity]]
  def deleteAll(): ConnectionIO[Int]
}

object GeoHashSQL {

  case class GeoHashEntity(latitude: Double, longitude: Double, geohash: String, uniquePrefix: String) {
    def toGeoHash: GeoHash =
      GeoHash(
        geoPoint = GeoPoint(latitude = Latitude(latitude), longitude = Longitude(longitude)),
        geoHash = GeoHashMaxPrecision(geohash),
        uniquePrefix = UniquePrefix(uniquePrefix)
      )
  }

  def make: GeoHashSQL[ConnectionIO] = new GeoHashSQL[ConnectionIO] {

    def insert(geoHash: GeoHash): ConnectionIO[Int] =
      sql"""
         INSERT
            INTO geohash (latitude, longitude, geohash, unique_prefix )
            VALUES (${geoHash.geoPoint.latitude.value}, 
                    ${geoHash.geoPoint.longitude.value}, 
                    ${geoHash.geoHash.value}, 
                    ${geoHash.uniquePrefix.value})
       """.update.run

    def deleteAll(): ConnectionIO[Int] =
      sql"""
        DELETE FROM geohash
       """.update.run

    def selectAll(page: Int, size: Int): ConnectionIO[List[GeoHashEntity]] =
      sql"""
        SELECT * FROM geohash LIMIT ${page},${size}
       """
        .query[GeoHashEntity]
        .to[List]

    def selectByGeoHash(geoHash: GeoHashMaxPrecision): ConnectionIO[Option[GeoHashEntity]] =
      sql"""
        SELECT * FROM geohash WHERE geohash=${geoHash.value}
       """
        .query[GeoHashEntity]
        .option
  }
}
