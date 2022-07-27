package jvd.model

sealed trait ValidationAction
case object UploadSchema     extends ValidationAction
case object DownloadSchema   extends ValidationAction
case object DeleteSchema     extends ValidationAction
case object ValidateDocument extends ValidationAction
