package com.stuart.geohash.infrastructure.ioc.provider

import cats.effect.kernel.Async
import com.stuart.geohash.domain.repositories.GeoHashRepository
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import com.stuart.geohash.infrastructure.repositories.{GeoHashMySqlRepository, GeoHashSQL}

sealed abstract class Repositories[F[_]] private (
  val geoHash: GeoHashRepository[F]
)

object Repositories {

  def make[F[_]: Async](mysql: MySqlClient[F]): Repositories[F] = new Repositories[F](
    geoHash = GeoHashMySqlRepository.make[F](
      mySqlClient = mysql,
      geoHashSQL = GeoHashSQL.make
    )
  ) {}
}
