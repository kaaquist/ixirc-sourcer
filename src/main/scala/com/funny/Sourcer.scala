package com.funny

import java.io.{PrintWriter, StringWriter}

import com.funny.utils.{Config, HttpHelper, WebSiteData}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


object Sourcer extends LazyLogging {
  val config = Config.theConf.ixIrcApiConf

  /**
   * Main created strictly for testing
   * @param args
   */
  def main(args: Array[String]): Unit = {
    logger.info("Here we go!!")
    val test = getData()
    val testt = Await.result(test, 1.minutes)
    logger.info(s"Here is the data:: ${testt.body}")
  }

  /**
   * Get data from ixirc Api
   * @return Future[WebSiteData]
   */
  def getData(): Future[WebSiteData] = {
    val ixircResult = for {
      domainData <- HttpHelper.get(s"${config.endpoint}")
    } yield domainData
    ixircResult recoverWith  {
      case e: Exception =>
        val message = e.getMessage
        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        logger.error(
          s"Exception in IxIrc data retrieval :: Message: $message :: Exception: ${sw.toString}"
        )
        Future.failed(throw new IllegalStateException(s"Exception in getting data from IxIrc or Parsing it as Json. :: ${sw.toString}"))
      case _ =>
        ixircResult
    }
  }
}
