package com.stuart.geohash.infrastructure.configuration

import cats.effect.kernel.Async
import cats.implicits._
import ciris.{default, Secret}
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
case class AppConfig(mysqlConfig: MysqlConfig)

import ciris.refined._
import eu.timepit.refined.cats._

case class MysqlConfig(
  jdbcUrl: NonEmptyString,
  user: NonEmptyString,
  password: Secret[NonEmptyString],
  maximumPoolSize: PosInt
)

object MysqlConfig {

  def fromNativeValues[F[_]: Async](
    jdbcUrl: String,
    user: String,
    password: String,
    maximumPoolSize: Int
  ): F[MysqlConfig] = (
    default(jdbcUrl).as[NonEmptyString],
    default(user).as[NonEmptyString],
    default(password).as[NonEmptyString].secret,
    default(maximumPoolSize).as[PosInt]
  ).parMapN { (jdbcUrlR, userR, passwordR, maximumPoolSizeR) =>
    MysqlConfig(
      jdbcUrl = jdbcUrlR,
      user = userR,
      password = passwordR,
      maximumPoolSize = maximumPoolSizeR
    )
  }.load[F]
}
