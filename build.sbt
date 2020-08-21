organization := "com.funny"
version := "0.0.1"

scalaVersion := "2.13.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.6.8"
  val AkkaHttpVersion = "10.1.11"
  val circeVersion = "0.13.0"
  Seq (
    "com.typesafe.scala-logging"                  %% "scala-logging"                                % "3.9.2",
    "ch.qos.logback"                              % "logback-classic"                               % "1.2.3",
    "org.scalatest"                               %% "scalatest"                                    % "3.2.0"     % "test",
    "com.github.pureconfig"                       %% "pureconfig"                                   % "0.11.1",
    "com.softwaremill.sttp"                       %% "okhttp-backend"                               % "1.6.0",
    "org.jsoup"                                   % "jsoup"                                         % "1.11.3",
    "org.json4s"                                  %% "json4s-jackson"                               % "3.6.7",
    "com.lightbend.akka"                          %% "akka-stream-alpakka-google-cloud-pub-sub"     % "2.0.1",
    "com.typesafe.akka"                           %% "akka-http"                                    % AkkaHttpVersion,
    "com.typesafe.akka"                           %% "akka-stream"                                  % akkaVersion,
    "com.typesafe.akka"                           %% "akka-actor"                                   % akkaVersion,
    "io.circe"                                    %% "circe-parser"                                 % circeVersion,
    "io.circe"                                    %% "circe-generic"                                % circeVersion,
    "io.circe"                                    %% "circe-core"                                   % circeVersion,
    "de.heikoseeberger"                           %% "akka-http-circe"                              % "1.33.0",
    "com.google.cloud"                            % "google-cloud-pubsub"                           % "1.108.1",
    "com.google.cloud"                            % "google-cloud-core"                             % "1.93.7",
    // Be aware that this here is a local dependency .. Use the following project to make it work:
    "com.funny" % "ixirc-schemas" % "0.0.1",

  )}

cancelable in Global := true
fork in run := true
connectInput in run := true

