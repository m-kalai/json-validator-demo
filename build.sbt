import Dependencies._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "cz.kalai"

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = (project in file("."))
  .settings(
    name := "Json Validator Demo",
    libraryDependencies ++= zio
  )
