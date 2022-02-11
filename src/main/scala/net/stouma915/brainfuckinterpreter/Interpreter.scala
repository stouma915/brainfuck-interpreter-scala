package net.stouma915.brainfuckinterpreter

import cats.effect.IO

object Interpreter {

  import cats.effect.unsafe.implicits.global

  def evaluate(
      sourceCode: String,
      memory: Memory
  ): IO[Either[String, (String, Memory)]] =
    IO {
      var mem = memory
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
                    val result = evaluate(loopCode, mem).unsafeRunSync()
                    result match {
                      case Right(x) =>
                        output += x._1
                        mem = x._2
                      case Left(err) =>
                        error = Some(err)
                    }
                  }

                  if (error.isEmpty) {
                    val result = evaluate(afterLoop, mem).unsafeRunSync()
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
