package com.funny.utils

import com.typesafe.scalalogging.LazyLogging
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, SnakeCase}
import pureconfig.generic.auto._

case class Config() extends LazyLogging {

  import Config._
  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, SnakeCase))

  lazy val prefixRoot = "funny"

  lazy val httpClientConf: HttpClientConf = pureconfig.loadConfigOrThrow[HttpClientConf](s"$prefixRoot.httpClient")
  lazy val ixIrcApiConf: IxIrcApiConf = pureconfig.loadConfigOrThrow[IxIrcApiConf](s"$prefixRoot.ixIrcApi")
  lazy val tickerConf: TickerConf = pureconfig.loadConfigOrThrow[TickerConf](s"$prefixRoot.tickerConfig")
  lazy val gcpConf: GCPConf = pureconfig.loadConfigOrThrow[GCPConf](s"$prefixRoot.gcpConfig")
}

object Config {
  val theConf: Config = Config()

  case class HttpClientConf(
                             userAgent: String,
                             timeout: Int
                           )
  case class IxIrcApiConf(
                         endpoint: String
                         )
  case class TickerConf(
                       waitDuration: Int
                       )
  case class GCPConf(
                      pubsubTopic: String,
                      batchMessageSize: Int,
                      googleServiceAccountEmail: String
                       )
}