package com.stuart.geohash.fixtures

import cats.effect.{IO, Resource}
import doobie.KleisliInterpreter
import doobie.util.transactor.{Strategy, Transactor}

import java.sql.Connection

trait DoobieFixture {

  lazy val conn: Option[Connection] = None
  lazy val fakeTransactor = Transactor(
    (),
    (_: Unit) => Resource.pure[IO, Connection](conn.orNull),
    KleisliInterpreter[IO].ConnectionInterpreter,
    Strategy.void
  )
}
