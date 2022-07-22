package jvd

import jvd.model.JSchema
import org.json4s.JObject
import zio.{Task, ZLayer}

trait ValidationService {
  def storeSchema(id: String, content: String): Task[Unit]
  def retrieveSchema(id: String): Task[JSchema]
  def validateSchema(id: String, content: String): Task[ValidationResult]
}

object ValidationServiceLive {
  val layer = ZLayer.fromFunction(ValidationServiceLive.apply _)
}

case class ValidationServiceLive(repository: SchemaRepository)
    extends ValidationService {
  override def storeSchema(id: String, content: String) =
    repository.insert(JSchema(id, JObject()))

  override def retrieveSchema(id: String) = ???

  override def validateSchema(id: String, content: String) = ???
}
