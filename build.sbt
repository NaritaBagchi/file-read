ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "Scala streaming practice"

lazy val rootFS = (project in file("."))
  .settings(
    name := "FS2Example",
    scalaSource in Compile := baseDirectory.value / "src",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "co.fs2" %% "fs2-core" % "1.0.0",
    libraryDependencies += "co.fs2" %% "fs2-io" % "1.0.0",
    libraryDependencies += "co.fs2" %% "fs2-reactive-streams" % "1.0.0",
    libraryDependencies += "co.fs2" %% "fs2-experimental" % "1.0.0",
    libraryDependencies += "com.typesafe" % "config" % "1.3.2"
  )