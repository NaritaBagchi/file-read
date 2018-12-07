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
import com.typesafe.config.ConfigUtil


object Launch {
  def main(args: Array[String]): Unit = {
    val st = System.currentTimeMillis()

    // L: typesafe config will by default read from application.conf
    // L: check the format of the conf file at github.
    val configContext = ConfigFactory.load()
    
    def localToLocal(configContext: Config) : List[Future[Unit]] = {
      import collection.JavaConversions._
      val b = ConfigUtil.splitPath("local_to_local.fileConfig.input")
      val x = configContext.getObjectList("local_to_local.fileConfig").toList.map((fileConfig) => {
        //inputFiles.map(file => Future(processFile(file, writeTo))).toList
        val inFile = fileConfig.get("input").unwrapped().toString
        val outFile = fileConfig.get("output").unwrapped().toString
        Future(processFile(inFile, outFile, writeTo))
      }).toList
      x
    }
    def localToAWS(configContext: Config) : List[Future[Unit]] = ???
    
    def execute(callback:(Config) => List[Future[Unit]], a: Config) = callback(a)
//    val futures =  configContext.getString("deployment_type") match {
//      case "local_to_local" => execute(localToLocal, configContext)
//      case "local_to_aws" => execute(localToAWS, configContext)
//    }
    val  futures = execute(localToLocal, configContext)
    Future.sequence(futures).onComplete {
      case Success(_) => println("success")
      case Failure(e) => println("failure: " + e.getMessage)
    }

    val timeTaken = System.currentTimeMillis() - st
    println(timeTaken / 1000)
  }

  def processFile[A](inFilename: String, outFilename: String, f: (Iterator[String], String) => Unit): Unit = {
    val inStream = Try(new FileInputStream(inFilename))
    val buffSrc = inStream.map(new io.BufferedSource(_))
    val buffItrRes = buffSrc.map(_.getLines).map(itr => {
      println("outFilename"+outFilename)
      f(itr, outFilename)
      })

    buffSrc.map(_.close)
    inStream.map(_.close)
  }

  def writeTo(buffSrcIterator: Iterator[String], outFilename: String) = {
    val pw = Try(new PrintWriter(outFilename))
    println("ndjkcn"+pw)
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