package com.scala.streaming.fs2

import java.io.FileInputStream
import scala.util.Try
import java.io.PrintWriter
import scala.util.Success
import scala.util.Failure

import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File
import scala.concurrent.Await

object Launch {
  def main(args: Array[String]): Unit = {
    val st = System.currentTimeMillis()
    val config = ConfigFactory.parseFile(new File("/Users/naritamitra/Study/Scala/FS2Example/src/main/resources/app.conf"))
    val inputFiles = config.getString("inputFiles").split(",")
    //val outputFile = config.getString("outputFile")

    val futures: List[Future[Unit]] = inputFiles.map(file => Future(processFile(file, writeTo))).toList
    Future.sequence(futures).onComplete {
      case Success(_) => println("success")
      case Failure(e) => println("failure: " + e.getMessage)
    }

    val timeTaken = System.currentTimeMillis() - st
    println(timeTaken / 1000)
  }

  def processFile[A](inFilename: String, f: (Iterator[String]) => Unit): Unit = {
    val inStream = Try(new FileInputStream(inFilename))
    val buffSrc = inStream.map(new io.BufferedSource(_))
    val buffItrRes = buffSrc.map(_.getLines).map(f)

    buffSrc.map(_.close)
    inStream.map(_.close)
  }

  def writeTo(buffSrcIterator: Iterator[String]) = {
    val pw = Try(new PrintWriter("celsiusout.txt"))
    pw match {
      case Success(pwf) => {
        while (buffSrcIterator.hasNext) {
          pwf write (buffSrcIterator.next)
          pwf write ("\n")
        }
      }
      case Failure(ex) => println("Exception occured", ex.getMessage)
    }
    pw.map(_.close)
  }
}