package com.scala.streaming.fs2

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.{ ExitCode, IO, IOApp, Resource }
import cats._
import cats.data._
import cats.implicits._
import fs2.io
import fs2.{ io, text, Stream }
import java.nio.file.Paths
import java.util.concurrent.Executors
// https://dumps.wikimedia.org/enwiki/latest/

object ExecuteApp extends IOApp {

  private val blockingExecutionContext =
    Resource.make(IO(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))))(ec => IO(ec.shutdown()))

  val converter: Stream[IO, Unit] = Stream.resource(blockingExecutionContext).flatMap { blockingEC =>

    io.file.readAll[IO](Paths.get("testdata/enwiki-latest-page.sql"), blockingEC, 4096)
      .through(text.utf8Decode)
      .through(text.lines)
      .filter(s => !s.trim.isEmpty && !s.startsWith("//"))
      .intersperse("\n")
      .through(text.utf8Encode)
      .through(io.file.writeAll(Paths.get("testdata/celsius.txt"), blockingEC))
  }

  def run(args: List[String]): IO[ExitCode] =
    converter.compile.drain.as(ExitCode.Success)
}