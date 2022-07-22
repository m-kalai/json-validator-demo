package jvd

import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import zio.{Task, ZIO}

import scala.io.Source

object Utils {
  def loadJson(fileName: String): Task[JValue] = for {
    json <- ZIO.acquireReleaseWith(
      ZIO.attempt(Source.fromResource(s"data/$fileName"))
    )(s => ZIO.succeed(s.close()))(source =>
      ZIO.attempt(JsonMethods.parse(source.reader()))
    )

  } yield json
}
