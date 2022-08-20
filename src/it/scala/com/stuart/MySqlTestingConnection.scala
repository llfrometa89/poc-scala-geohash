package com.stuart

import cats.effect.IO
import com.stuart.geohash.infrastructure.configuration.MysqlConfig
import com.stuart.geohash.infrastructure.db.client.MySqlClient

trait MySqlTestingConnection {

  lazy val mysqlConfigTest: IO[MysqlConfig] = MysqlConfig.fromNativeValues[IO](
    jdbcUrl = "jdbc:mysql://localhost:9006/stuart",
    user = "root",
    password = "root",
    maximumPoolSize = 5
  )

  lazy val mySqlClientTest: IO[MySqlClient[IO]] = for {
    mySqlConfig <- mysqlConfigTest
    mySqlClient <- IO.delay(MySqlClient.make[IO](mySqlConfig))
  } yield mySqlClient
}
