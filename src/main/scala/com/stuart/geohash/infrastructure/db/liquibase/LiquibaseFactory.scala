package com.stuart.geohash.infrastructure.db.liquibase

import cats.effect.Sync
import cats.implicits._
import com.stuart.geohash.infrastructure.db.client.MySqlClient
import liquibase.database.jvm.JdbcConnection
import liquibase.database.{Database, DatabaseFactory}
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{Contexts, LabelExpression, Liquibase}

import java.sql.Connection

trait LiquibaseFactory[F[_]] {
  def update(): F[Unit]
}

object LiquibaseFactory {

  def make[F[_]: Sync](mySqlClient: MySqlClient[F]): LiquibaseFactory[F] = new LiquibaseFactory[F] {

    val ChangelogFile = "classpath:/db/changelog/changelog.xml"

    private def mkDatabase(connection: Connection): F[Database] =
      Sync[F].delay(DatabaseFactory.getInstance.findCorrectDatabaseImplementation(new JdbcConnection(connection)))

    def update(): F[Unit] = for {
      database  <- mkDatabase(mySqlClient.getDataSource.getConnection)
      liquibase <- Sync[F].delay(new Liquibase(ChangelogFile, new ClassLoaderResourceAccessor, database))
      _         <- Sync[F].delay(liquibase.update(new Contexts, new LabelExpression))
    } yield ()
  }
}
