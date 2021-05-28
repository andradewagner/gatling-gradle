package br.com.util

import com.amazonaws.{AmazonServiceException, SdkClientException}

import java.io.{File, FileInputStream, FileOutputStream}
import java.text.SimpleDateFormat
import java.util.Date
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3._
import com.amazonaws.services.s3.model.{DeleteObjectRequest, GetObjectRequest, ListObjectsV2Request, ListObjectsV2Result, ObjectMetadata, PutObjectRequest, S3Object, S3ObjectSummary}
import jodd.io.ZipUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.RandomStringUtils

import java.util
import java.util.zip.ZipOutputStream
import scala.util.Properties

trait AWSStorageClient {

  val jwtPath: String = Properties.envOrElse("PERF_TEST_JWT_PATH", "key.json")
  val bucket: String = Properties.envOrElse("PERF_TEST_RESULTS_BUCKET_NAME", "testesamwagnand")
  val fileNameSuffix: String = Properties.envOrElse("HOSTNAME", RandomStringUtils.random(10, true, true))
  val downloadPath: String = Properties.envOrElse("PERF_TEST_LOG_DOWNLOAD_PATH", "build/reports/downloadedLogs")
  val clientRegion: Regions = Regions.DEFAULT_REGION
  var s3Object: S3Object = null

  val storageClient: AmazonS3 = AmazonS3ClientBuilder
    .standard()
    .withRegion(Regions.US_EAST_1)
    .build()
}

trait fsUtils {

  def getListOfDirs(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isDirectory).toList
    } else {
      List[File]()
    }
  }

}

object LogUploader extends AWSStorageClient with fsUtils {

  def main(args: Array[String]): Unit = {

    val fileList = getListOfDirs("build/reports/gatling")
    val reportDir = fileList.head
    println("Diretorio listado: " + reportDir)
    println("Sufixo gerado: " + fileNameSuffix)
    try {
      val request: PutObjectRequest = new PutObjectRequest(bucket, s"simulation-$fileNameSuffix.log", new File(s"$reportDir/simulation.log"))
      val metadata: ObjectMetadata = new ObjectMetadata()
      metadata.setContentType("plain/text")
      metadata.addUserMetadata("title", "reportGatling")
      request.setMetadata(metadata)

      storageClient.putObject(request)
    }
    catch {
      case ex: AmazonServiceException => {
        ex.printStackTrace()
      }
      case ex: SdkClientException => {
        ex.printStackTrace()
      }
    }
  }
}

object LogDownloader extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    new File(downloadPath).mkdirs()

    try {
      val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      val listObjectResult: ListObjectsV2Result = s3.listObjectsV2(bucket)
      val objects: java.util.List[S3ObjectSummary] = listObjectResult.getObjectSummaries

      val it: util.Iterator[S3ObjectSummary] = objects.iterator()

      while(it.hasNext) {
        val os: S3ObjectSummary = it.next()
        if(os.getKey.endsWith(".log")) {
          println("Baixando log " + os.getKey)
          s3Object = storageClient.getObject(new GetObjectRequest(bucket, os.getKey))
          ReportSaver.save(s3Object)
        }
        System.out.printf(" - %s (size: %d)\n", os.getKey, os.getSize)
      }
    }
    catch {
      case ex: AmazonServiceException => {
        ex.printStackTrace()
      }
      case ex: SdkClientException => {
        ex.printStackTrace()
      }
    }
  }
}

object ReportSaver extends AWSStorageClient {

  def save(s3Object: S3Object): Unit = {

    val target: File = new File(downloadPath + "/" + s3Object.getKey)
    FileUtils.copyInputStreamToFile(s3Object.getObjectContent, target)
  }
}

object ReportUploader extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val date: Date = new Date
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("ddMMyyyy-HH-mm-ss")
    val filename: String = dateFormat.format(date)

    val zipFile = new FileOutputStream(s"build/reports/$filename.zip")
    val zos: ZipOutputStream = new ZipOutputStream(zipFile)
    ZipUtil.addFolderToZip(zos, downloadPath, "")

    try {
      val request: PutObjectRequest = new PutObjectRequest(bucket, zipFile.toString, new File(zipFile.toString))
      val metadata: ObjectMetadata = new ObjectMetadata()
      metadata.setContentType("text/plain;charset=UTF-8")
      metadata.addUserMetadata("title", "reportGatlingZip")
      request.setMetadata(metadata)

      storageClient.putObject(request)
    }
    catch {
      case ex: AmazonServiceException => {
        ex.printStackTrace()
      }
      case ex: SdkClientException => {
        ex.printStackTrace()
      }
    }
    println("Uploaded report " + zipFile.toString)
  }

}

object LogDeleter extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    try {
      val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      val listObjectResult: ListObjectsV2Result = s3.listObjectsV2(bucket)
      val objects: java.util.List[S3ObjectSummary] = listObjectResult.getObjectSummaries

      val it: util.Iterator[S3ObjectSummary] = objects.iterator()

      while(it.hasNext) {
        val os: S3ObjectSummary = it.next()
        if(os.getKey.endsWith(".log")) {
          println("Apagando log " + os.getKey)
          storageClient.deleteObject(new DeleteObjectRequest(bucket, os.getKey))
        }
        System.out.printf("Apagando Log - %s (size: %d)\n", os.getKey, os.getSize)
      }
    }
    catch {
      case ex: AmazonServiceException => {
        ex.printStackTrace()
      }
      case ex: SdkClientException => {
        ex.printStackTrace()
      }
    }
  }
}