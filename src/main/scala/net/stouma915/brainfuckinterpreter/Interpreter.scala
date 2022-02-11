package net.stouma915.brainfuckinterpreter

import cats.effect.IO

object Interpreter {

  import cats.effect.unsafe.implicits.global

  def evaluate(sourceCode: String, memory: Memory): IO[Either[String, String]] =
    IO {
      var previousMemory = memory

      val outputsOrErrors = sourceCode.toCharArray
        .map { c =>
          IO {
            val result = eval(c, previousMemory).unsafeRunSync()

            result match {
              case Right(x) =>
                previousMemory = x._2
                Right(x._1)
              case Left(err) =>
                Left(err)
            }
          }
        }
        .map(_.unsafeRunSync())
        .toList

      if (outputsOrErrors.exists(_.isLeft))
        Left(outputsOrErrors.filter(_.isLeft).head.left.getOrElse(""))
      else
        Right(outputsOrErrors.map(_.getOrElse("")).mkString(""))
    }

  private def eval(
      character: Char,
      memory: Memory
  ): IO[Either[String, (String, Memory)]] =
    IO {
      character match {
        case '+' =>
          Right("", memory.incrementValue)
        case '-' =>
          Right("", memory.decrementValue)
        case '>' =>
          Right("", memory.increment)
        case '<' =>
          Right("", memory.decrement)
        case '.' =>
          Right(ASCIIConverter.convert(memory.getCurrentValue).toString, memory)
        case _ =>
          Right("", memory)
      }
    }

}
