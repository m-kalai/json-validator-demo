package jvd

import jvd.model.JSchema
import zio.test.{ZIOSpecDefault, assertTrue}

object ValidatorSpec extends ZIOSpecDefault {
  override def spec = suite("ValidatorSpec")(
    test("validator returns validation success") {
      for {
        schema <- Utils.loadJson("config-schema.json")
        doc    <- Utils.loadJson("config.json")
        res    <- Validator.validate(JSchema("config-schema", schema), doc)
      } yield assertTrue(res == ValidationSuccess)
    },
    test("validator returns validation failure") {
      for {
        schema <- Utils.loadJson("config-schema.json")
        doc    <- Utils.loadJson("config-invalid.json")
        res    <- Validator.validate(JSchema("config-schema", schema), doc)
      } yield assertTrue(
        res == ValidationError(
          "object has missing required properties ([\"destination\"])"
        )
      )
    }
  )
}
