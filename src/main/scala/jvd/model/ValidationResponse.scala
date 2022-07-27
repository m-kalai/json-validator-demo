package jvd.model

import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.jackson.Serialization
import zhttp.http.{Response, Status}

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
