package net.stouma915.brainfuckinterpreter

import cats.effect.{ExitCode, IO}
import net.stouma915.brainfuckinterpreter.Util

import scala.util.{Success, Try}
import java.io.File
import java.nio.file.{Files, Paths}

object BrainfuckInterpreter {

  private[brainfuckinterpreter] def showHelp(): IO[Unit] =
    for {
      _ <- IO {
        println("Brainf**k Interpreter 1.0.0")
        println("")
      }
      _ <- showUsage()
      _ <- IO {
        println("""
                  |Flags:
                  |    -h, --help    Prints help information.
                  |    
                  |Args:
                  |    <SOURCE> Brainfuck source file.
                  |""".stripMargin)
      }
    } yield ()

  private[brainfuckinterpreter] def showUsage(): IO[Unit] =
    for {
      _ <- IO(println("Usage:"))
      progName <- Util.getProgramName(BrainfuckInterpreter.getClass)
      _ <- IO(println(s"    $progName [FLAGS] [SOURCE]"))
    } yield ()

}

@main
def interpreter(args: String*): Unit = {

  import cats.effect.unsafe.implicits.global
  import BrainfuckInterpreter.*

  def exit(exitCode: ExitCode) = IO(sys.exit(exitCode.code))

  val program = IO {
    if (args.length != 1) {
      val program = for {
        _ <- showHelp()
        _ <- exit(ExitCode.Error)
      } yield ()

      program.unsafeRunSync()
    } else {
      val sourceFileName = args.head

      if (sourceFileName.startsWith("-")) {
        sourceFileName match {
          case "--help" =>
            val program = for {
              _ <- showHelp()
              _ <- exit(ExitCode.Success)
            } yield ()

            program.unsafeRunSync()
          case "-h" =>
            val program = for {
              _ <- showHelp()
              _ <- exit(ExitCode.Success)
            } yield ()

            program.unsafeRunSync()
          case _ =>
            val program = for {
              _ <- IO {
                println(
                  s"Found argument '$sourceFileName' which wasn't expected, or isn't valid in this context."
                )
                println("")
              }
              _ <- showUsage()
              _ <- IO {
                println("")
                println("For more information try --help")
              }
              _ <- exit(ExitCode.Error)
            } yield ()

            program.unsafeRunSync()
        }
      } else {
        Try(Files.readString(Paths.get(sourceFileName)))
          .map { source =>
            IO {
              val sourceWithoutReturns = source.replaceAll("\n", "")

              implicit val _memory: Memory = Memory.Empty

              val result =
                Interpreter
                  .evaluate(sourceWithoutReturns)
                  .unsafeRunSync()

              result match {
                case Right(x) =>
                  val program = for {
                    _ <- IO(println(x._1))
                    _ <- exit(ExitCode.Success)
                  } yield ()

                  program.unsafeRunSync()
                case Left(err) =>
                  val program = for {
                    _ <- IO {
                      println("Execution failed.")
                      println(err)
                    }
                    _ <- exit(ExitCode.Error)
                  } yield ()

                  program.unsafeRunSync()
              }
            }
          }
          .recoverWith { case e: Exception =>
            Success {
              for {
                _ <- IO(println(s"Unable to read source file: $e"))
                _ <- exit(ExitCode.Error)
              } yield ()
            }
          }
          .get
          .unsafeRunSync()
      }
    }
  }

  program.unsafeRunSync()
}
