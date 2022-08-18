import Dependencies._

lazy val commonScalacOptions = Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Wunused:imports,patvars,locals",
  "-Wnumeric-widen",
  "-Xlint:-unused",
  "-Ymacro-annotations",
  "-language:implicitConversions"
)

lazy val root = project
  .in(file("."))
  .settings(
    name         := "geohash",
    organization := "com.stuart",
    version      := "1.0.0",
    scalaVersion := "2.13.8",
    libraryDependencies ++= dependencies,
    libraryDependencies ++= testDependencies,
    scalacOptions ++= commonScalacOptions
  )
  .configs(IntegrationTest)
  .settings(
    inConfig(IntegrationTest)(Defaults.itSettings),
    IntegrationTest / fork               := true,
    IntegrationTest / testForkedParallel := true
  )

lazy val dependencies = Seq(
  Libraries.cats,
  Libraries.catsEffect,
  Libraries.log4cats,
  Libraries.refinedCore,
  Libraries.refinedCats,
  Libraries.logback,
  Libraries.newtype,
  Libraries.doobieCore,
  Libraries.doobieHikari,
  Libraries.doobieH2,
  Libraries.doobieScalatest,
  Libraries.mysqlConnectorJava,
  Libraries.geohash,
  Libraries.commonsCli,
  Libraries.circeCore,
  Libraries.circeGeneric,
  Libraries.circeParser,
  Libraries.circeRefined
)

lazy val testDependencies = Seq(
  Libraries.scalaTest,
  Libraries.refinedScalacheck,
  Libraries.scalaTestCheck,
  Libraries.mockitoScala,
  Libraries.testContainers
)
