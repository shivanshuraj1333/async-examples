import sbt._

object Dependencies {
  object Versions {
    val scala = "2.12.18"
    val scalaLogging = "3.9.5"
    val logback = "1.4.11"
    val scopt = "4.1.0"
    val collectionCompat = "2.11.0"
    val scalatest = "3.2.17"
    val mockito = "5.8.0"
    val slf4j = "2.0.9"
  }

  val core = Seq(
    "org.scala-lang" % "scala-library" % Versions.scala,
    "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging,
    "ch.qos.logback" % "logback-classic" % Versions.logback,
    "org.slf4j" % "slf4j-api" % Versions.slf4j,
    "com.github.scopt" %% "scopt" % Versions.scopt,
    "org.scala-lang.modules" %% "scala-collection-compat" % Versions.collectionCompat,
    "org.scala-lang.modules" %% "scala-java8-compat" % "1.0.2"
  )

  val testing = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalatest % Test,
    "org.mockito" % "mockito-core" % Versions.mockito % Test,
    "org.scalatest" %% "scalatest-flatspec" % Versions.scalatest % Test,
    "ch.qos.logback" % "logback-classic" % Versions.logback % Test
  )

  val all = core ++ testing
} 