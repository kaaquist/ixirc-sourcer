package com.funny

import java.io.{PrintWriter, StringWriter}
import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorSystem, Props}
import com.funny.release.{ReleaseMessage, Releases}
import com.funny.utils.{Config, WebSiteData}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import io.circe._
import io.circe.parser._

import scala.concurrent.Future

class ApiDataHandler extends Actor with LazyLogging {
  val formatReleaseDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  def receive = {
    case WebData(data) =>
      logger.info(s"ApiDataHandler about to extract data ...")
      data andThen {
        case Success(value) =>
          val jsonLine:Json = parse(value.body).getOrElse(Json.Null)
          val cursor = jsonLine.hcursor
          val results = cursor.downField("results").values.head
          logger.debug(s"Data size :: ${results.size}, first element :: ${results.head}")
          val releaseMessages = results.map(f => {
            // Need to make sure that we have time in mills.
            val date = new java.util.Date(f.\\("age").head.toString().toLong * 1000L)
            logger.debug(s"Name :: ${f.\\("name").head}, Age :: ${f.\\("age").head} and Human data :: ${formatReleaseDate.format(date)}")
            ReleaseMessage(
                name = f.\\("name").head.toString(),
                releaseDateUnixTimestamp = f.\\("age").head.toString().toLong,
                releaseDate = formatReleaseDate.format(date)
            )
          }).toSeq
          val releases = Releases(releaseMessages)
          logger.debug(s"This here is the results :: $releases")
        case Failure(ex) =>
          val sw = new StringWriter
          ex.printStackTrace(new PrintWriter(sw))
          logger.error(s"An error occurred: ${sw.toString}")
      }

    case _ => println(s"ApiDataHandler got some other message.")
  }
}

case class WebData(webSiteData: Future[WebSiteData])
/**
 * Created a ticker class to trigger a call to a web api every x sec.
 */
object Ticker extends App with LazyLogging {
  val simpleDateFormat = new SimpleDateFormat("hh:mm:ss")
  val tickerConf = Config.theConf.tickerConf
  val Tick = "tick"
  class TickActor extends Actor {
    def receive = {
      case Tick =>
        val data = Sourcer.getData()
        logger.debug(s"${simpleDateFormat.format(new Date(System.currentTimeMillis()))} :: $Tick")
        apiDataHandler ! WebData(data)
    }
  }
  val system = ActorSystem()

  val tickActor = system.actorOf(Props(classOf[TickActor]))
  val apiDataHandler = system.actorOf(Props[ApiDataHandler], name = "ApiDataHandler")
  // Create a ticker that ticks every 1 minute.
  val cancellable = system.scheduler.scheduleWithFixedDelay(Duration.Zero, tickerConf.waitDuration.second, tickActor, Tick)

  logger.info(s"Ticker started...\nPress RETURN to stop...")
  scala.io.StdIn.readLine()
  logger.info(s"We got a RETURN stop request.")
  //This cancels further Ticks to be sent
  cancellable.cancel()
  system.terminate()
  // Killing the system that keeps the thread hanging.
  // TODO : Need to find out why the program keeps hanging after system termination. Might be related to okHttpClient.
  sys.exit()
}
