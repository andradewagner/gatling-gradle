package br.com.util

import java.io.{File, FileInputStream}
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import org.zeroturnaround.zip.ZipUtil
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3._

import scala.util.Properties

trait AWSStorageClient {

  val jwtPath: String = Properties.envOrElse("PERF_TEST_JWT_PATH", "key.json")
  val bucket: String = Properties.envOrElse("PERF_TEST_RESULTS_BUCKET_NAME", "YOUR_BUCKET_NAME")
  val fileNameSuffix: String = Properties.envOrElse("HOSTNAME", randomGenerators.randomAlphanumericString(10))
  val downloadPath: String = Properties.envOrElse("PERF_TEST_LOG_DOWNLOAD_PATH", "build/reports/downloadedLogs")
  val awsCreds: BasicAWSCredentials = new BasicAWSCredentials("ACCESS_KEY", "SECRET_KEY")
  val jwtStream = new FileInputStream(new File(jwtPath))

  val credentials: AWSStaticCredentialsProvider = try {
    new AWSStaticCredentialsProvider(awsCreds) //fromStream(jwtStream)
  } finally {
    jwtStream.close()
  }

  val storageClient: AmazonS3 = AmazonS3ClientBuilder.standard().withCredentials(credentials).build()
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

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val fileList = getListOfDirs("build/reports/gatling")
    val reportDir = fileList.head

    storageClient.create(
      BlobInfo.newBuilder(bucket, s"simulation-$fileNameSuffix.log").build(),
      new FileInputStream(s"$reportDir/simulation.log")
    )
  }

}

object LogDownloader extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val blobs = storageClient.list(bucket, BlobListOption.currentDirectory, BlobListOption.prefix(""))
    new File(downloadPath).mkdirs()

    blobs.iterateAll().forEach(blob => {
      if (blob.getName.endsWith(".log")) {
        println("Downloading log " + blob.getName)
        blob.downloadTo(Paths.get(downloadPath + "/" + blob.getName))
      }
    })
  }

}

object ReportUploader extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val reportDir = new File(downloadPath)

    val date: Date = new Date
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss")
    val filename: String = dateFormat.format(date)

    val zipFile = new File(s"build/reports/$filename.zip")
    ZipUtil.pack(reportDir, zipFile)

    storageClient.create(
      BlobInfo.newBuilder(bucket, "reports/" + zipFile.getName).build(),
      new FileInputStream(zipFile)
    )
    println("Uploaded report " + zipFile.getName)
  }

}

object LogDeleter extends AWSStorageClient with fsUtils {

  @throws[Exception]
  def main(args: Array[String]): Unit = {

    val blobs = storageClient.list(bucket, BlobListOption.currentDirectory, BlobListOption.prefix(""))

    blobs.iterateAll().forEach(blob => {
      if (blob.getName.endsWith(".log")) {
        println("Deleting logfile " + blob.getName)
        blob.delete()
      }
    })
  }

}