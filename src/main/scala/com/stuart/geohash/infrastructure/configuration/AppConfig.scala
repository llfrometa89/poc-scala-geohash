package com.stuart.geohash.infrastructure.configuration

import ciris.Secret
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

case class AppConfig(mysqlConfig: MysqlConfig)

case class MysqlConfig(
  JdbcUrl: NonEmptyString,
  user: NonEmptyString,
  password: Secret[NonEmptyString],
  maximumPoolSize: PosInt
)
