import sbt._

object Dependencies {
  object Version {
    lazy val zioCore = "2.0.0"
  }

  lazy val zio = Seq(
    "dev.zio" %% "zio"          % Version.zioCore,
    "dev.zio" %% "zio-macros"   % Version.zioCore,
    "dev.zio" %% "zio-test"     % Version.zioCore % Test,
    "dev.zio" %% "zio-test-sbt" % Version.zioCore % Test
  )

  lazy val rocksDb = Seq(
    "dev.zio"    %% "zio-rocksdb" % "0.4.0",
    "org.rocksdb" % "rocksdbjni"  % "6.29.5"
  )

  lazy val http = Seq(
    "io.d11" %% "zhttp" % "2.0.0-RC10"
  )

  lazy val json = Seq(
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14",
    "org.json4s"                %% "json4s-jackson"        % "4.0.5"
  )
}
