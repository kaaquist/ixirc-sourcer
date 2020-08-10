package com.funny.utils

import java.io.{PrintWriter, StringWriter}

import com.funny.release.Releases
import com.google.cloud.ServiceOptions
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.{ProjectTopicName, PubsubMessage}
import com.typesafe.scalalogging.LazyLogging
import com.google.protobuf.ByteString

import scala.util.Try

case class PubSubException(message:String, t: Throwable) extends Exception(message, t)

object PubSubPublisher extends LazyLogging {
  private val projectId = ServiceOptions.getDefaultProjectId()
  private val gcpConf = Config().gcpConf
  private val topicName = ProjectTopicName.of(projectId, gcpConf.pubsubTopic)
  private val publisher = Publisher.newBuilder(topicName).build()

  def publishMessage(releases:Releases): Unit ={
    Try {
      publisher
        .publish(
          PubsubMessage
            .newBuilder()
            .setData(ByteString.copyFrom(releases.toByteArray))
            .build()
        )
        .get()
    } recoverWith {
      case ex: Exception =>
        val sw = new StringWriter
        ex.printStackTrace(new PrintWriter(sw))
        logger.error(s"error publishing to pubsub with topic: $topicName, " +
          s"Exception : ${sw.toString}, ")
        throw PubSubException("publish releases error", ex)
    }
  }
}
