package example

import zio.ZIOAppDefault

object Hello extends ZIOAppDefault {
  override def run = zio.Console.printLine("Hello!")
}
