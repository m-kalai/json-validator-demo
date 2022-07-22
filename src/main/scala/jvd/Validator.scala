package jvd

import com.github.fge.jsonschema.main.JsonSchemaFactory
import jvd.model.JSchema
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.asJsonNode
import zio.{Task, ZIO}
import scala.jdk.CollectionConverters._

sealed trait ValidationResult
case object ValidationSuccess               extends ValidationResult
case class ValidationError(message: String) extends ValidationResult

object Validator {
  private val factory = JsonSchemaFactory.byDefault()

  def validate(schema: JSchema, json: JValue): Task[ValidationResult] =
    ZIO.attempt {
      val s      = factory.getJsonSchema(asJsonNode(schema.body))
      val report = s.validate(asJsonNode(json.noNulls))
      if (report.isSuccess) {
        ValidationSuccess
      } else {
        ValidationError(report.iterator().asScala.map(_.getMessage).mkString)
      }
    }
}
