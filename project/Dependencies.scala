import sbt._

object Dependencies {
  object Version {
    lazy val zio = "2.0.0"
  }

  lazy val zio = Seq(
    "dev.zio" %% "zio"          % Version.zio,
    "dev.zio" %% "zio-test"     % Version.zio % Test,
    "dev.zio" %% "zio-test-sbt" % Version.zio % Test
  )
}
