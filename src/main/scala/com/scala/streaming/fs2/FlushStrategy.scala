package com.scala.streaming.fs2

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import cats.implicits._
import fs2.io
import fs2.{ io, text, Stream }
import java.nio.file.Paths
import java.util.concurrent.Executors

import java.io.File
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.ExecutionContextExecutorService

trait FlushStrategy {
  def flush 
}

class FlushToLocalStrategy extends FlushStrategy {
  def flush = ???
}

class FlushToS3Strategy extends FlushStrategy {
  def flush: Unit = {
    ???
  }
}
