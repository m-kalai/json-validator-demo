package jvd

import com.fasterxml.jackson.core.JsonParseException
import jvd.model.ValidationResponse.toResponse
import jvd.model._
import org.json4s.jackson.JsonMethods
import zhttp.http.{Response, Status}
import zio.macros.accessible
import zio.{UIO, ZLayer}

@accessible
trait ValidationHandler {
  def uploadSchema(id: String, content: String): UIO[Response]
  def downloadSchema(id: String): UIO[Response]
  def deleteSchema(id: String): UIO[Response]
  def validateDocument(id: String, content: String): UIO[Response]
}

object ValidationHandlerLive {
  val layer = ZLayer.fromFunction(ValidationHandlerLive.apply _)
}

case class ValidationHandlerLive(service: ValidationService)
    extends ValidationHandler {
  override def uploadSchema(id: String, content: String) =
    service
      .storeSchema(id, content)
      .fold(
        e => {
          val res = ValidationResponse
            .upload(id)
            .copy(
              status = Failure,
              message = Option(e.getMessage)
            )

          e match {
            case _: JsonParseException =>
              res.copy(statusCode = Some(Status.BadRequest))
            case _: ValidationService.Conflict =>
              res.copy(statusCode = Some(Status.Conflict))
            case _ => res
          }
        },
        _ =>
          ValidationResponse
            .upload(id)
            .copy(status = Success, statusCode = Some(Status.Created))
      )
      .map(toResponse)

  override def downloadSchema(id: String) = service
    .retrieveSchema(id)
    .fold(
      _ => ValidationResponse.download(id).copy(status = Failure).toResponse,
      {
        case Some(value) => Response.json(JsonMethods.compact(value.body))
        case None =>
          ValidationResponse
            .download(id)
            .copy(
              status = Failure,
              message = Some("Schema not found"),
              statusCode = Some(Status.NotFound)
            )
            .toResponse
      }
    )

  override def validateDocument(id: String, content: String) = service
    .validateSchema(id, content)
    .fold(
      _ => ValidationResponse.validate(id).copy(status = Failure),
      vr => {
        val res = ValidationResponse.validate(id)
        vr match {
          case ValidationSuccess => res.copy(status = Success)
          case ValidationError(message) =>
            res.copy(status = Failure, message = Some(message))
        }
      }
    )
    .map(toResponse)

  override def deleteSchema(id: String) = service
    .deleteSchema(id)
    .fold(
      _ => ValidationResponse.delete(id).copy(status = Failure),
      _ => ValidationResponse.delete(id).copy(status = Success)
    )
    .map(toResponse)
}
