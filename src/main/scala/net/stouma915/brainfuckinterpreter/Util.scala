package net.stouma915.brainfuckinterpreter

object Util {

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
