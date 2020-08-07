package com.funny

import java.io.{PrintWriter, StringWriter}
import java.text.SimpleDateFormat

import akka.actor.Actor
import com.funny.release.{ReleaseMessage, Releases}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.parser.parse

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import utils._

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
              releaseDateUnixTimestamp = f.\\("age").head.toString.toLong,
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