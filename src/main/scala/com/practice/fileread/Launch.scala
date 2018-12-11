package com.practice.fileread

import java.io.{ FileInputStream, PrintWriter, File }
import scala.util.{ Try, Success, Failure }

import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.{ Future, Await }
import scala.concurrent.ExecutionContext.Implicits.global
import com.typesafe.config.ConfigUtil
import scala.concurrent.duration.Duration

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
//import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.model.PutObjectRequest

object Launch {
  def main(args: Array[String]): Unit = {

    // Learnings: typesafe config will by default read from application.conf
    // Learnings: check the format of the conf file at github.
    val configContext = ConfigFactory.load()

    val st = System.currentTimeMillis()
    def execute(callback: (Config) => List[Future[Unit]], a: Config) = callback(a)

    def localToLocal(configContext: Config): List[Future[Unit]] = {
      import collection.JavaConversions._
      configContext.getObjectList("local_to_local.fileConfig").toList.map((fileConfig) => {
        val inFile = fileConfig.get("input").unwrapped().toString
        val outFile = fileConfig.get("output").unwrapped().toString
        Future(processFile(inFile, outFile, writeTo))
      }).toList
    }
    def localToAWS(configContext: Config): List[Future[Unit]] = {
      import collection.JavaConversions._
      configContext.getObjectList("local_to_aws.fileConfig").toList.map((fileConfig) => {
        val inFile = fileConfig.get("input").unwrapped().toString
        val outFile = fileConfig.get("output").unwrapped().toString
        Future(processFile(inFile, outFile, writeToAWS)) // DEcouple all the layers - reuse the input layer and pat match only on the output
      }).toList
    }

    val futures = configContext.getString("deployment_type") match {
      case "local_to_local" => execute(localToLocal, configContext)
      case "local_to_aws"   => execute(localToAWS, configContext)
    }
    Await.result(Future.sequence(futures), Duration.Inf)

    val timeTaken = System.currentTimeMillis() - st
    println(timeTaken / 1000)
  }

  def writeToAWS(buffSrcIterator: Iterator[String], outFilename: String) = {
    val clientRegion = ""
    val bucketName = ""
    val fileObjKeyName = ""

    val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
      .withRegion(clientRegion)
      .withCredentials(new ProfileCredentialsProvider())
      .build()

    val request = new PutObjectRequest(bucketName, fileObjKeyName, new File(outFilename))
    val metadata = new ObjectMetadata()
    metadata.setContentType("plain/text")
    metadata.addUserMetadata("x-amz-meta-title", "")
    request.setMetadata(metadata)

    // Upload a text string as a new object.
    // Try(s3Client.putObject(bucketName, stringObjKeyName, "Uploaded String Object")) match {
    Try(s3Client.putObject(request)) match {
      case Success(_)  => println("Successfully uploaded the file to S3")
      case Failure(ex) => println("Ex" + ex.getMessage)
    }

    //    val pw = new PrintWriter(outFilename)
    //    while (buffSrcIterator.hasNext) {
    //      pw write (buffSrcIterator.next)
    //      pw write ("\n")
    //    }
    //    pw.close
  }

  def processFile(inFilename: String, outFilename: String, f: (Iterator[String], String) => Unit): Unit = {
    val inStream = Try(new FileInputStream(inFilename))
    val buffSrc = inStream.map(new scala.io.BufferedSource(_)) // take buffer size
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