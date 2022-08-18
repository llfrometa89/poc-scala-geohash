package com.stuart.geohash.infrastructure.configuration

import cats.effect.Async
import cats.syntax.all._
import ciris._
import ciris.refined._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

object Config {

  def load[F[_]: Async]: F[AppConfig] =
    (
      env("DB_MYSQL_JDBC_URL").as[NonEmptyString],
      env("DB_MYSQL_USER").as[NonEmptyString],
      env("DB_MYSQL_PASSWORD").as[NonEmptyString].secret,
      env("DB_MYSQL_MAXIMUM_POOL_SIZE").as[PosInt]
    ).parMapN { (dbMysqlJdbcUrl, dbMysqlUser, dbMysqlPassword, dbMysql) =>
      AppConfig(
        mysqlConfig = MysqlConfig(dbMysqlJdbcUrl, dbMysqlUser, dbMysqlPassword, dbMysql)
      )
    }.load[F]
}
