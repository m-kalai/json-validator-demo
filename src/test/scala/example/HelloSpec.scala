package example

import zio.test.{TestConsole, ZIOSpecDefault, assertTrue}

object HelloSpec extends ZIOSpecDefault {
  override def spec = suite("HelloSpec") {
    test("app displays correct output") {
      for {
        _      <- Hello.run
        output <- TestConsole.output
      } yield assertTrue(output == Vector("Hello!\n"))
    }
  }
}
