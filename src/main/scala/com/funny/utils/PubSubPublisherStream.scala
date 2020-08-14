package com.funny.utils

import java.util.Base64

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.alpakka.googlecloud.pubsub.scaladsl.GooglePubSub
import akka.stream.alpakka.googlecloud.pubsub.{PubSubConfig, PublishMessage, PublishRequest}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.funny.release.Releases
import com.google.cloud.ServiceOptions

case class PubSubPublisherStream()(implicit actorSystem: ActorSystem) {
  // >>**BE AWARE**<< this here assume that we have set google application credentials as an environment variable.
  private[this] val privateKey:String = sys.env.getOrElse("GOOGLE_SERVICE_ACCOUNT_CREDENTIALS", "WRONG CREDENTIALS")
  private[this] val gcpConfig = Config.theConf.gcpConf
  private[this] val projectId = ServiceOptions.getDefaultProjectId
  private[this] val config = PubSubConfig(projectId, gcpConfig.googleServiceAccountEmail, privateKey)

  // TODO: Create this here as a batch sink or its like. For more info check here: https://doc.akka.io/docs/alpakka/current/google-cloud-pub-sub.html
  def source(request: PublishRequest): Source[PublishRequest, NotUsed] = Source.single(request)

  def publishFlow(pubSubTopic: String): Flow[PublishRequest, Seq[String], NotUsed] =
    GooglePubSub.publish(pubSubTopic, config)

  def publish(pubSubTopic: String, releases: Releases): Unit = {
    val publishMessage =
      PublishMessage(new String(Base64.getEncoder.encode(releases.toByteArray)))
    val publishRequest = PublishRequest(Seq(publishMessage))
    source(publishRequest).via(publishFlow(pubSubTopic)).runWith(Sink.seq)
  }
}
