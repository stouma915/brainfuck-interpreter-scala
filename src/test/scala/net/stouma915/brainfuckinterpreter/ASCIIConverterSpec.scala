package net.stouma915.brainfuckinterpreter

import net.stouma915.brainfuckinterpreter.ASCIIConverter.convert
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.*

class ASCIIConverterSpec extends AnyFlatSpec with should.Matchers {

  "convert" should "Convert Int to ASCII correctly." in {
    convert(65) should be('A')
    convert(66) should be('B')
    convert(67) should be('C')

    convert(0) should be('\u0000')
    convert(1) should be('\u0001')

    List(72, 101, 108, 108, 111).map(convert).mkString("") should be("Hello")
  }

}
