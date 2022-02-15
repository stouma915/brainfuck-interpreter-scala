package net.stouma915.brainfuckinterpreter

import net.stouma915.brainfuckinterpreter.Interpreter.evaluate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*

class InterpreterSpec extends AnyFlatSpec with should.Matchers {

  import cats.effect.unsafe.implicits.global

  implicit val _emptyMemory: Memory = Memory.Empty

  "evaluate" should "Evaluate the Brainfuck source code." in {
    evaluate("----[---->+<]>++.").unsafeRunSync() should be(
      Right("A", Memory(1, Map(0 -> 0, 1 -> 65)))
    )
    evaluate("++++[++++>---<]>-.").unsafeRunSync() should be(
      Right("B", Memory(1, Map(0 -> 0, 1 -> 66)))
    )
    evaluate("++++[++++>---<]>.").unsafeRunSync() should be(
      Right("C", Memory(1, Map(0 -> 0, 1 -> 67)))
    )

    evaluate("-[------->+<]>-.-[->+++++<]>++.+++++++..+++.")
      .unsafeRunSync() should be(
      Right("Hello", Memory(2, Map(0 -> 0, 1 -> 0, 2 -> 111)))
    )
  }

  it should "Return an error for the wrong code." in {
    evaluate("[").unsafeRunSync() should be(
      Left("The end of the loop couldn't be identified.")
    )
    evaluate("[[[[[]]]]").unsafeRunSync() should be(
      Left("The end of the loop couldn't be identified.")
    )
  }

}
