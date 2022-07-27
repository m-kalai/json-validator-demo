package jvd

import zhttp.http._
import zhttp.service.Server
import zio.ZIOAppDefault
import zio.rocksdb.RocksDB

object Main extends ZIOAppDefault {

  val app: Http[ValidationHandler, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "schema" / schemaId =>
        ValidationHandler.downloadSchema(schemaId)
      case req @ Method.POST -> !! / "schema" / schemaId =>
        req.bodyAsString.flatMap(body =>
          ValidationHandler.uploadSchema(schemaId, body)
        )
      case Method.DELETE -> !! / "schema" / schemaId =>
        ValidationHandler.deleteSchema(schemaId)
      case req @ Method.POST -> !! / "validate" / schemaId =>
        req.bodyAsString.flatMap(body =>
          ValidationHandler.validateDocument(schemaId, body)
        )
    }

  override def run =
    Server
      .start(9000, app)
      .provide(
        ValidationHandlerLive.layer,
        ValidationServiceLive.layer,
        SchemaRepository.live,
        RocksDB.live("schema-db")
      )
}
