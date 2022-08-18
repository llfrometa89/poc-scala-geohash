import sbt._

object Dependencies {

  object V {
    val cats               = "2.8.0"
    val catsEffect         = "3.3.14"
    val log4cats           = "2.3.1"
    val doobie             = "1.0.0-RC2"
    val mysqlConnectorJava = "8.0.30"
    val refined            = "0.9.29"
    val newtype            = "0.4.4"
    val logback            = "1.2.11"
    val scalaTest          = "3.2.13"
    val mockitoScala       = "1.14.4"
    val testContainers     = "1.17.2"
    val geohash            = "1.4.0"
    val scalaTestCheck     = "3.2.2.0"
    val commonsCli         = "1.5.0"
  }

  object Libraries {

    def doobie(artifact: String): ModuleID = "org.tpolecat" %% s"doobie-$artifact" % V.doobie

    val cats       = "org.typelevel" %% "cats-core"      % V.cats
    val catsEffect = "org.typelevel" %% "cats-effect"    % V.catsEffect
    val log4cats   = "org.typelevel" %% "log4cats-slf4j" % V.log4cats

    val doobieCore      = doobie("core")
    val doobieHikari    = doobie("hikari")
    val doobieH2        = doobie("h2")
    val doobieScalatest = doobie("scalatest")

    val mysqlConnectorJava = "mysql" % "mysql-connector-java" % V.mysqlConnectorJava

    val refinedCore       = "eu.timepit" %% "refined"            % V.refined
    val refinedCats       = "eu.timepit" %% "refined-cats"       % V.refined
    val refinedScalacheck = "eu.timepit" %% "refined-scalacheck" % V.refined % "test, it"

    val newtype = "io.estatico" %% "newtype" % V.newtype
    val geohash = "ch.hsr"       % "geohash" % V.geohash

    lazy val commonsCli = "commons-cli" % "commons-cli" % V.commonsCli

    // Runtime
    val logback = "ch.qos.logback" % "logback-classic" % V.logback

    // Test
    val scalaTest      = "org.scalatest"     %% "scalatest"       % V.scalaTest      % "test, it"
    val scalaTestCheck = "org.scalatestplus" %% "scalacheck-1-14" % V.scalaTestCheck % "test, it"
    val mockitoScala   = "org.mockito"       %% "mockito-scala"   % V.mockitoScala   % "test, it"
    val testContainers = "org.testcontainers" % "testcontainers"  % V.testContainers % "it"

  }
}
