package net.stouma915.brainfuckinterpreter

import cats.effect.IO

import java.util.Scanner

object Interpreter {

  import cats.effect.unsafe.implicits.global

  def evaluate(
      sourceCode: String
  )(implicit memory: Memory): IO[Either[String, (String, Memory)]] =
    IO {
      implicit var mem: Memory = memory
      var output = ""
      var error: Option[String] = None

      var stop = false

      sourceCode.zipWithIndex.foreach { case (char, index) =>
        val program = IO {
          char match {
            case '+' =>
              mem = mem.incrementValue
            case '-' =>
              mem = mem.decrementValue
            case '>' =>
              mem = mem.increment
            case '<' =>
              mem = mem.decrement
            case '.' =>
              output += ASCIIConverter.convert(mem.getCurrentValue).toString
            case ',' =>
              val scanner = new Scanner(System.in)

              var done = false
              var input = 0

              while (!done) {
                print("Input was requested: ")

                val line = scanner.nextLine()

                line.toIntOption match {
                  case Some(x) =>
                    if (x >= -128 && x <= 127) {
                      input = x
                      done = true
                    } else println("Please enter a 1 byte number.")
                  case None =>
                    println("Please enter a 1 byte number.")
                }
              }

              mem = mem.setValue(input)
            case '[' =>
              val codeBeforeBracket = sourceCode.substring(0, index)
              val codeAfterBracket =
                sourceCode.substring(index, sourceCode.length)

              val loopEndIndex =
                Util.searchLoopEnd(codeBeforeBracket, codeAfterBracket)
              loopEndIndex match {
                case Some(i) =>
                  val loopCode = sourceCode.substring(index + 1, i - 1)
                  val afterLoop = sourceCode.substring(i, sourceCode.length)

                  while (mem.getCurrentValue != 0) {
                    val result = evaluate(loopCode).unsafeRunSync()
                    result match {
                      case Right(x) =>
                        output += x._1
                        mem = x._2
                      case Left(err) =>
                        error = Some(err)
                    }
                  }

                  if (error.isEmpty) {
                    val result = evaluate(afterLoop).unsafeRunSync()
                    result match {
                      case Right(x) =>
                        output += x._1
                        mem = x._2
                      case Left(err) =>
                        error = Some(err)
                    }
                  }

                  stop = true
                case None =>
                  error = Some("The end of the loop couldn't be identified.")
              }
            case _ =>
          }
        }

        if (!stop)
          program.unsafeRunSync()
      }

      if (error.isEmpty)
        Right(output, mem)
      else
        Left(error.get)
    }

}
