package com.stuart.geohash.infrastructure.db.client

import cats.effect.{Async, Resource}
import cats.implicits._
import com.stuart.geohash.infrastructure.configuration.MysqlConfig
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor

import javax.sql.DataSource

trait MySqlClient[F[_]] {
  def getDataSource: DataSource
  def transactor: F[Transactor[F]]
  def transactorR: Resource[F, HikariTransactor[F]]
}

object MySqlClient {

  def make[F[_]: Async](mysqlConfig: MysqlConfig): MySqlClient[F] = new MySqlClient[F] {

    val config = new HikariConfig()
    config.setJdbcUrl(mysqlConfig.JdbcUrl.value)
    config.setUsername(mysqlConfig.user.value)
    config.setPassword(mysqlConfig.password.value.value)
    config.setMaximumPoolSize(mysqlConfig.maximumPoolSize.value)

    lazy val dataSource: HikariDataSource = new HikariDataSource(config)

    def getDataSource: DataSource = dataSource

    def transactor: F[Transactor[F]] = for {
      ec         <- Async[F].executionContext
      transactor <- Async[F].pure(HikariTransactor.apply[F](dataSource, ec))
    } yield transactor

    def transactorR: Resource[F, HikariTransactor[F]] = for {
      ec         <- ExecutionContexts.fixedThreadPool[F](5)
      transactor <- HikariTransactor.fromHikariConfig[F](dataSource, ec)
    } yield transactor
  }
}
