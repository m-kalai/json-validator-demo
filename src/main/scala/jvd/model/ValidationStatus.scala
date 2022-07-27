package jvd.model

sealed trait ValidationStatus
case object Success extends ValidationStatus
case object Failure extends ValidationStatus
case object Unknown extends ValidationStatus
