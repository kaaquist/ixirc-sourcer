package com.funny.utils

import java.security.SecureRandom
import java.security.cert.X509Certificate

import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{SttpBackend, _}

import javax.net.ssl.X509TrustManager
import com.typesafe.scalalogging.LazyLogging
import javax.net.ssl.{SSLContext, TrustManager}
import okhttp3.{Dispatcher, Interceptor, OkHttpClient}

import scala.concurrent.Future
import scala.concurrent.duration.SECONDS
import scala.util.Try

import scala.concurrent.ExecutionContext.Implicits.global


case class WebSiteData(body: String, error: Boolean, errorMsg: String)

object HttpHelper extends LazyLogging {
  private val conf = Config()

  val trustAllCerts: Array[TrustManager] = Array[TrustManager](new X509TrustManager() {
    def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {}
    def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {}
    def getAcceptedIssuers = new Array[X509Certificate](0)
  })

  def makeSttpBackend(maxRequests: Int) = {
    val dispatcher = new Dispatcher()
    dispatcher.setMaxRequests(maxRequests)
    dispatcher.setMaxRequestsPerHost(maxRequests)
    val sc:SSLContext = SSLContext.getInstance("TLSv1.2")

    sc.init(null, trustAllCerts, new SecureRandom())

    val client = new OkHttpClient.Builder()
      .dispatcher(dispatcher)
      .addInterceptor((chain: Interceptor.Chain) => {
        val ongoing = chain.request().newBuilder()
        ongoing.addHeader("User-Agent", conf.httpClientConf.userAgent)
        ongoing.addHeader("Accept-Language", "en;q=1, en-GB;q=0.8, da;q=0.7")
        chain.proceed(ongoing.build())
      })
      .sslSocketFactory(sc.getSocketFactory, trustAllCerts.head.asInstanceOf[X509TrustManager])
      .followRedirects(true)
      .followSslRedirects(true)
      .hostnameVerifier((_,_) => true)
      .callTimeout(conf.httpClientConf.timeout, SECONDS)
      .build()
    val backend = OkHttpFutureBackend.usingClient(client)

    backend
  }

  def get(domain: String): Future[WebSiteData] = {
    val uri = Future(uri"$domain")
    implicit val backend = makeSttpBackend(1)
    val response = uri.map(sttp.get).flatMap(_.send())

    val result =
      response map {
        case res if res.is200 =>
          val body = Try(res.unsafeBody).getOrElse("")
          WebSiteData(body = body, error = false, errorMsg = "")
        case res if res.isServerError =>
          res.rawErrorBody match {
            case Left(value) =>
              WebSiteData(body = null, error = true, errorMsg = s"$value")
            case Right(value) =>
              WebSiteData(body = null, error = true, errorMsg = s"$value")
          }
      }
    result
  }
}
