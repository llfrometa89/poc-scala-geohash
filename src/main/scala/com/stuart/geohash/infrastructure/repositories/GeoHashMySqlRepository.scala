package com.stuart.geohash.infrastructure.repositories

import cats.effect.kernel.Async
import com.stuart.geohash.domain.models.geohash._
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import cats.implicits._

object GeoHashMySqlRepository {

  def make[F[_]: Async](mySqlClient: MySqlClient[F]): GeoHashRepository[F] = new GeoHashRepository[F] {

    def create(geoHash: GeoHash): F[GeoHash] = for {
      _ <- mySqlClient.transactor.use(transactor => mkExecute(GeoHashSQL.insert(geoHash), transactor))
    } yield geoHash

    def findBy(geoHashMaxPrecision: GeoHashMaxPrecision): F[Option[GeoHash]] =
      for {
        mEntity <- mySqlClient.transactor.use(transactor =>
          mkExecute(GeoHashSQL.selectByGeoHash(geoHashMaxPrecision.value), transactor)
        )
        geoHash <- mEntity.map(_.toGeoHash).pure[F]
      } yield geoHash

    def findAll(page: Long, size: Long): F[List[GeoHash]] =
      for {
        entities  <- mySqlClient.transactor.use(transactor => mkExecute(GeoHashSQL.selectAll(page, size), transactor))
        geoHashes <- entities.map(_.toGeoHash).pure[F]
      } yield geoHashes

    private def mkExecute[A](connectionIO: ConnectionIO[A], transactor: HikariTransactor[F]): F[A] =
      connectionIO.transact(transactor)
  }
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

  def selectByGeoHash(geoHash: String): doobie.ConnectionIO[Option[GeoHashEntity]] =
    sql"""
        SELECT * FROM geohash WHERE geohash=${geoHash}
       """
      .query[GeoHashEntity]
      .option

}
