package com.funny

import java.text.SimpleDateFormat
import java.util.Date
import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
// The utils in the project.
import utils._

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
  val apiDataHandler = system.actorOf(Props(classOf[ApiDataHandler]), name = "ApiDataHandler")
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
