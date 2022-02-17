package net.stouma915.brainfuckinterpreter

import cats.effect.IO

import java.io.File

object Util {

  def getProgramName(c: Class[_]): IO[String] = IO {
    new File(
      c.getProtectionDomain.getCodeSource.getLocation.getPath
    ).getName
  }

  def searchLoopEnd(before: String, after: String): Option[Int] = {
    var result: Option[Int] = None

    var countOfBracket = 0
    var countOfClosingBracket = 0

    after.zipWithIndex.foreach { case (char, index) =>
      if (result.isEmpty) {
        if (char == '[')
          countOfBracket += 1
        else if (char == ']')
          countOfClosingBracket += 1

        if (countOfBracket == countOfClosingBracket) {
          result = Some((index + 1) + before.length)
        }
      }
    }

    result
  }

}
