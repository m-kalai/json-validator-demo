package jvd

import zhttp.http.Response
import zio.macros.accessible
import zio.{TaskLayer, UIO, ZLayer}

sealed trait ValidationAction
case object UploadSchema     extends ValidationAction
case object DownloadSchema   extends ValidationAction
case object ValidateDocument extends ValidationAction

sealed trait ValidationStatus
case object Success extends ValidationStatus
case object Failure extends ValidationStatus

case class ValidationResponse(
    action: ValidationAction,
    id: String,
    status: ValidationStatus,
    message: Option[String]
)

@accessible
trait ValidationHandler {
  def uploadSchema(id: String, content: String): UIO[Response]
  def downloadSchema(id: String): UIO[Response]
  def validateDocument(id: String, content: String): UIO[Response]
}

object ValidationHandlerLive {
  val layer = ZLayer.fromFunction(ValidationHandlerLive.apply _)
}

case class ValidationHandlerLive(service: ValidationService)
    extends ValidationHandler {
  override def uploadSchema(id: String, content: String) = ???

  override def downloadSchema(id: String) = ???

  override def validateDocument(id: String, content: String) = ???
}
