package com.stuart.geohash.infrastructure.ioc.provider

import cats.effect.kernel.Async
import com.stuart.geohash.infrastructure.configuration.AppConfig
import com.stuart.geohash.infrastructure.db.client.MySqlClient

sealed abstract class AppResources[F[_]](
  val mysql: MySqlClient[F]
)

object AppResources {

  def make[F[_]: Async](config: AppConfig): AppResources[F] = {
    val mysql = MySqlClient.make[F](config.mysqlConfig)
    new AppResources[F](mysql) {}
  }
}
