package jvd

import jvd.model.JSchema
import org.json4s.jackson.JsonMethods
import zio.rocksdb.RocksDB
import zio.{Task, ZLayer}

import java.nio.charset.StandardCharsets

trait SchemaRepository {
  def insert(schema: JSchema): Task[Unit]
  def get(id: String): Task[Option[JSchema]]
  def delete(id: String): Task[Unit]
}

object SchemaRepository {
  val live = ZLayer.fromFunction(RocksDBSchemaRepository.apply _)
}

case class RocksDBSchemaRepository(rocksDB: RocksDB) extends SchemaRepository {
  private val charset = StandardCharsets.UTF_8

  override def insert(schema: JSchema) =
    rocksDB.put(
      schema.id.getBytes(charset),
      JsonMethods.compact(schema.body).getBytes(charset)
    )

  override def get(id: String) = for {
    content <- rocksDB.get(id.getBytes(charset))
  } yield content
    .map(b => JsonMethods.parse(new String(b, charset)))
    .map(JSchema(id, _))

  override def delete(id: String) = rocksDB.delete(id.getBytes(charset))
}
