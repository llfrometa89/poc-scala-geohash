package com.stuart.geohash.infrastructure.db.client

import cats.effect.Async
import cats.implicits._
import com.stuart.geohash.infrastructure.configuration.MysqlConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.hikari.HikariTransactor

trait MySqlClient[F[_]] {
  def getDataSource: HikariDataSource
  def transactor: F[HikariTransactor[F]]
}

object MySqlClient {

  def make[F[_]: Async](mysqlConfig: MysqlConfig): MySqlClient[F] = new MySqlClient[F] {

    val config = new HikariConfig()
    config.setJdbcUrl(mysqlConfig.JdbcUrl.value)
    config.setUsername(mysqlConfig.user.value)
    config.setPassword(mysqlConfig.password.value.value)
    config.setMaximumPoolSize(mysqlConfig.maximumPoolSize.value)

    lazy val dataSource: HikariDataSource = new HikariDataSource(config)

    def getDataSource: HikariDataSource = dataSource

    def transactor: F[HikariTransactor[F]] = for {
      ec         <- Async[F].executionContext
      transactor <- Async[F].pure(HikariTransactor.apply[F](dataSource, ec))
    } yield transactor

  }
}
