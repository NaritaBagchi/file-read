package com.practice.fileread

import java.io.{ FileInputStream, PrintWriter, File }
import scala.util.{ Try, Success, Failure }

import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.{ Future, Await }
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigUtil
import scala.concurrent.duration.Duration

object Launch {
  def main(args: Array[String]): Unit = {
    val st = System.currentTimeMillis()

    // L: typesafe config will by default read from application.conf
    // L: check the format of the conf file at github.
    val configContext = ConfigFactory.load()
    def execute(callback: (Config) => List[Future[Unit]], a: Config) = callback(a)

    def localToLocal(configContext: Config): List[Future[Unit]] = {
      import collection.JavaConversions._
      configContext.getObjectList("local_to_local.fileConfig").toList.map((fileConfig) => {
        val inFile = fileConfig.get("input").unwrapped().toString
        val outFile = fileConfig.get("output").unwrapped().toString
        Future(processFile(inFile, outFile, writeTo))
      }).toList
    }
    def localToAWS(configContext: Config): List[Future[Unit]] = ???

    val futures = configContext.getString("deployment_type") match {
      case "local_to_local" => execute(localToLocal, configContext)
      case "local_to_aws"   => execute(localToAWS, configContext)
    }
    Await.result(Future.sequence(futures), Duration.Inf)

    val timeTaken = System.currentTimeMillis() - st
    println(timeTaken / 1000)
  }

  def processFile(inFilename: String, outFilename: String, f: (Iterator[String], String) => Unit): Unit = {
    val inStream = Try(new FileInputStream(inFilename))
    val buffSrc = inStream.map(new io.BufferedSource(_))
    val buffItrRes = buffSrc.map(_.getLines).map(itr => f(itr, outFilename))
    buffSrc.map(_.close)
    inStream.map(_.close)
  }

  def writeTo(buffSrcIterator: Iterator[String], outFilename: String) = {
    val pw = new PrintWriter(outFilename)
    while (buffSrcIterator.hasNext) {
      pw write (buffSrcIterator.next)
      pw write ("\n")
    }
    pw.close
  }
}