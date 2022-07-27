package jvd

import jvd.ValidationService.Conflict
import jvd.model.JSchema
import org.json4s.jackson.JsonMethods
import zio.{Task, ZIO, ZLayer}

trait ValidationService {
  def storeSchema(id: String, content: String): Task[Unit]
  def retrieveSchema(id: String): Task[Option[JSchema]]

  def deleteSchema(id: String): Task[Unit]
  def validateSchema(id: String, content: String): Task[ValidationResult]
}

object ValidationService {
  case class Conflict(message: String) extends Throwable(message)
}

object ValidationServiceLive {
  val layer = ZLayer.fromFunction(ValidationServiceLive.apply _)
}

case class ValidationServiceLive(repository: SchemaRepository)
    extends ValidationService {
  override def storeSchema(id: String, content: String) = for {
    json  <- ZIO.attempt(JsonMethods.parse(content))
    check <- repository.get(id)
    _ <-
      if (check.isDefined) ZIO.fail(Conflict(s"Schema $id already exists."))
      else repository.insert(JSchema(id, json))
  } yield ()

  override def retrieveSchema(id: String) = repository.get(id)

  override def validateSchema(id: String, content: String) = for {
    schema <- repository.get(id).map(_.get)
    doc    <- ZIO.attempt(JsonMethods.parse(content))
    res    <- Validator.validate(schema, doc)
  } yield res

  override def deleteSchema(id: String) = repository.delete(id)
}
