package com.stuart.geohash.infrastructure.db.client

import cats.effect.{Async, Resource}
import com.stuart.geohash.infrastructure.configuration.MysqlConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor

trait MySqlClient[F[_]] {
  def transactor: Resource[F, HikariTransactor[F]]
}

object MySqlClient {

  def make[F[_]: Async](mysqlConfig: MysqlConfig): MySqlClient[F] = new MySqlClient[F] {

    val hikariConfig = new HikariConfig()
    hikariConfig.setJdbcUrl(mysqlConfig.JdbcUrl.value)
    hikariConfig.setUsername(mysqlConfig.user.value)
    hikariConfig.setPassword(mysqlConfig.password.value.value)
    hikariConfig.setMaximumPoolSize(mysqlConfig.maximumPoolSize.value)

    def transactor: Resource[F, HikariTransactor[F]] = for {
      ec         <- ExecutionContexts.fixedThreadPool(5)
      transactor <- HikariTransactor.fromHikariConfig[F](new HikariDataSource(hikariConfig), ec)
    } yield transactor
  }
}
