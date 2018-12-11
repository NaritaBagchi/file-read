ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "Scala streaming practice"

lazy val rootFS = (project in file("."))
  .settings(
    name := "File-IO-AWS",
    scalaSource in Compile := baseDirectory.value / "src",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "com.typesafe" % "config" % "1.3.2",
    libraryDependencies +=  "com.amazonaws" % "aws-java-sdk" % "1.11.465"
  )