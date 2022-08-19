package com.stuart.geohash.infrastructure.repositories

import cats.effect.kernel.Async
import cats.implicits._
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import com.stuart.geohash.infrastructure.repositories.GeoHashSQL.GeoHashEntity
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

object GeoHashMySqlRepository {

  def make[F[_]: Async](mySqlClient: MySqlClient[F], geoHashSQL: GeoHashSQL): GeoHashRepository[F] =
    new GeoHashRepository[F] {

      def create(geoHash: GeoHash): F[GeoHash] = for {
        ax <- mySqlClient.transactor
        _  <- mkExecute(geoHashSQL.insert(geoHash), ax)
      } yield geoHash

      def findBy(geoHashMaxPrecision: GeoHashMaxPrecision): F[Option[GeoHash]] =
        for {
          ax      <- mySqlClient.transactor
          mEntity <- mkExecute(geoHashSQL.selectByGeoHash(geoHashMaxPrecision), ax)
          geoHash <- mEntity.map(_.toGeoHash).pure[F]
        } yield geoHash

      def findAll(page: Long, size: Long): F[List[GeoHash]] =
        for {
          ax        <- mySqlClient.transactor
          entities  <- mkExecute(geoHashSQL.selectAll(page, size), ax)
          geoHashes <- entities.map(_.toGeoHash).pure[F]
        } yield geoHashes

      private def mkExecute[A](connectionIO: ConnectionIO[A], transactor: Transactor[F]): F[A] =
        connectionIO.transact(transactor)
    }
}

trait GeoHashSQL {
  def insert(geoHash: GeoHash): doobie.ConnectionIO[Int]
  def selectAll(page: Long, size: Long): doobie.ConnectionIO[List[GeoHashEntity]]
  def selectByGeoHash(geoHash: GeoHashMaxPrecision): doobie.ConnectionIO[Option[GeoHashEntity]]
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

  def make: GeoHashSQL = new GeoHashSQL {

    def insert(geoHash: GeoHash): doobie.ConnectionIO[Int] =
      sql"""
         INSERT
            INTO geohash (latitude, longitude, geohash, unique_prefix )
            VALUES (${geoHash.geoPoint.latitude.value}, 
                    ${geoHash.geoPoint.latitude.value}, 
                    ${geoHash.geoHash.value}, 
                    ${geoHash.uniquePrefix.value})
       """.update.run

    def selectAll(page: Long, size: Long): doobie.ConnectionIO[List[GeoHashEntity]] =
      sql"""
        SELECT * FROM geohash LIMIT ${page},${size}
       """
        .query[GeoHashEntity]
        .to[List]

    def selectByGeoHash(geoHash: GeoHashMaxPrecision): doobie.ConnectionIO[Option[GeoHashEntity]] =
      sql"""
        SELECT * FROM geohash WHERE geohash=${geoHash.value}
       """
        .query[GeoHashEntity]
        .option
  }
}
