package br.com.util

import org.junit.Test
import scala.util.Properties

trait AWSStorageClientTest {

  val jwtPath: String = Properties.envOrElse("PERF_TEST_JWT_PATH", "key.json")
  val bucket: String = Properties.envOrElse("PERF_TEST_RESULTS_BUCKET_NAME", "YOUR_BUCKET_NAME")
  val fileNameSuffix: String = Properties.envOrElse("HOSTNAME", "")
  val downloadPath: String = Properties.envOrElse("PERF_TEST_LOG_DOWNLOAD_PATH", "build/reports/downloadedLogs")
  val accessKey: String = Properties.envOrElse("ACCESS_KEY", "chave AWS")
  val secretKey: String = Properties.envOrElse("SECRET_KEY", "senha AWS")


}

@Test
object LogUploaderTest extends AWSStorageClientTest {

  def main(args: Array[String]): Unit = {
    assert(jwtPath.equals("key.json"))
  }
}

@Test
object LogDownloaderTest extends AWSStorageClientTest {

  def main(args: Array[String]): Unit = {

  }
}

@Test
object ReportSaverTest extends AWSStorageClientTest {

  def save(s3Object: String): Unit = {

  }
}