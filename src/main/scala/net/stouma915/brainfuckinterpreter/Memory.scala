package net.stouma915.brainfuckinterpreter

object Memory {

  final val Empty = Memory(0, Map.empty)

}

case class Memory(pointer: Int, entries: Map[Int, Int]) {

  def increment: Memory = Memory(pointer + 1, entries)

  def decrement: Memory = Memory(if (pointer > 0) pointer - 1 else 0, entries)

  def incrementValue: Memory = {
    val newValue = entries.applyOrElse(pointer, _ => 0) + 1
    if (newValue >= 256)
      Memory(pointer, entries + (pointer -> 0))
    else
      Memory(pointer, entries + (pointer -> newValue))
  }

  def decrementValue: Memory = {
    val newValue = entries.applyOrElse(pointer, _ => 256) - 1
    if (newValue <= -1)
      Memory(pointer, entries + (pointer -> 255))
    else
      Memory(pointer, entries + (pointer -> newValue))
  }

  def setValue(value: Int): Memory = {
    var mem = Memory(pointer, entries + (pointer -> 0))

    (0 to math.abs(value)).foreach { _ =>
      if (value < 0)
        mem = mem.decrementValue
      else
        mem = mem.incrementValue
    }

    mem
  }

  def getCurrentValue: Int = entries.applyOrElse(pointer, _ => 0)

}
