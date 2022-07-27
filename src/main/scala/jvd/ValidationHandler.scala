package jvd

import com.fasterxml.jackson.core.JsonParseException
import jvd.ValidationResponse.toResponse
import org.json4s.jackson.{JsonMethods, Serialization}
import org.json4s.{DefaultFormats, FieldSerializer}
import zhttp.http.{Response, Status}
import zio.macros.accessible
import zio.{UIO, ZLayer}

sealed trait ValidationAction
case object UploadSchema     extends ValidationAction
case object DownloadSchema   extends ValidationAction
case object DeleteSchema     extends ValidationAction
case object ValidateDocument extends ValidationAction

sealed trait ValidationStatus
case object Success extends ValidationStatus
case object Failure extends ValidationStatus
case object Unknown extends ValidationStatus

case class ValidationResponse(
    action: ValidationAction,
    id: String,
    status: ValidationStatus,
    message: Option[String] = None,
    statusCode: Option[Status] = None
) {

  def isSuccess: Boolean = status == Success

  def toResponse: Response = ValidationResponse.toResponse(this)

}

object ValidationResponse {
  def upload(id: String): ValidationResponse =
    ValidationResponse(UploadSchema, id, Unknown)

  def download(id: String): ValidationResponse =
    ValidationResponse(DownloadSchema, id, Unknown)

  def validate(id: String): ValidationResponse =
    ValidationResponse(ValidateDocument, id, Unknown)

  def delete(id: String): ValidationResponse =
    ValidationResponse(DeleteSchema, id, Unknown)

  private implicit val formats =
    DefaultFormats + FieldSerializer[ValidationResponse](serializer = {
      case ("action", value: ValidationAction) =>
        Option(
          (
            "action",
            value match {
              case UploadSchema     => "uploadSchema"
              case DownloadSchema   => "downloadSchema"
              case DeleteSchema     => "deleteSchema"
              case ValidateDocument => "validateDocument"
            }
          )
        )
      case ("status", value: ValidationStatus) =>
        Option(
          (
            "status",
            value match {
              case Success => "success"
              case Failure => "error"
              case Unknown => "unknown"
            }
          )
        )
      case ("statusCode", _) => None
    })

  def toResponse(r: ValidationResponse): Response = {
    val res = Response.json(Serialization.write(r))
    r.statusCode match {
      case Some(value) => res.setStatus(value)
      case None =>
        if (r.isSuccess) {
          res
        } else {
          res.setStatus(Status.BadRequest)
        }
    }
  }
}

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
