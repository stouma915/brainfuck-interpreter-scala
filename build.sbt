ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "3.1.1"
ThisBuild / description := "Brainf**kインタプリタ"

libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "BrainfuckInterpreter",
    assemblyOutputPath / assembly := baseDirectory.value / "target" / "build" / s"${name.value}-${version.value}.jar"
  )
