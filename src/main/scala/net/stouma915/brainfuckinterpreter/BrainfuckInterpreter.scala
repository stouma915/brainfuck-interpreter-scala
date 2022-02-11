package net.stouma915.brainfuckinterpreter

import cats.effect.{ExitCode, IO, IOApp, Resource}

import scala.util.{Success, Try}
import java.io.File
import java.nio.file.{Files, Paths}

object BrainfuckInterpreter extends IOApp {

  import cats.effect.unsafe.implicits.global

  override def run(args: List[String]): IO[ExitCode] =
    IO {
      if (args.length != 1) {
        showHelp().unsafeRunSync()

        ExitCode.Error
      } else {
        val sourceFileName = args.head

        if (sourceFileName.startsWith("-")) {
          sourceFileName match {
            case "--help" =>
              showHelp().unsafeRunSync()

              ExitCode.Success
            case "-h" =>
              showHelp().unsafeRunSync()

              ExitCode.Success
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
              } yield ()
              program.unsafeRunSync()

              ExitCode.Error
          }
        } else {
          Try(Files.readString(Paths.get(sourceFileName)))
            .map { source =>
              IO {
                val sourceWithoutReturns = source.replaceAll("\n", "")

                val result =
                  Interpreter
                    .evaluate(sourceWithoutReturns, Memory.Empty)
                    .unsafeRunSync()

                result match {
                  case Right(x) =>
                    println(x._1)

                    ExitCode.Success
                  case Left(err) =>
                    println("Execution failed.")
                    println(err)

                    ExitCode.Error
                }
              }
            }
            .recoverWith { case e: Exception =>
              Success {
                IO {
                  println(s"Unable to read source file: $e")

                  ExitCode.Error
                }
              }
            }
            .get
            .unsafeRunSync()
        }
      }
    }

  private def showHelp(): IO[Unit] = IO {
    println("Brainf**k Interpreter 1.0.0")
    println("")
    showUsage().unsafeRunSync()
    println("""
        |Flags:
        |    -h, --help    Prints help information.
        |    
        |Args:
        |    <SOURCE> Brainfuck source file.
        |""".stripMargin)
  }

  private def showUsage(): IO[Unit] = IO {
    println("Usage:")
    println(s"    ${getProgramName.unsafeRunSync()} [FLAGS] [SOURCE]")
  }

  private def getProgramName: IO[String] = IO {
    new File(
      BrainfuckInterpreter.getClass.getProtectionDomain.getCodeSource.getLocation.getPath
    ).getName
  }

}
