name := "ixirc-sourcer"
organization := "com.funny"
version := "0.0.1"

scalaVersion := "2.13.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.6.8"
  val circeVersion = "0.13.0"
  Seq (
    "com.typesafe.scala-logging"                  %% "scala-logging"                    % "3.9.2",
    "ch.qos.logback"                              % "logback-classic"                   % "1.2.3",
    "org.scalatest"                               %% "scalatest"                        % "3.2.0"     % "test",
    "com.github.pureconfig"                       %% "pureconfig"                       % "0.11.1",
    "com.softwaremill.sttp"                       %% "okhttp-backend"                   % "1.6.0",
    "org.jsoup"                                   % "jsoup"                             % "1.11.3",
    "org.json4s"                                  %% "json4s-jackson"                   % "3.6.7",
    "com.typesafe.akka"                           %% "akka-http"                        % "10.1.11",
    "com.typesafe.akka"                           %% "akka-stream"                      % akkaVersion,
    "com.typesafe.akka"                           %% "akka-actor"                       % akkaVersion,
    "io.circe"                                    %% "circe-parser"                     % circeVersion,
    "io.circe"                                    %% "circe-generic"                    % circeVersion,
    "io.circe"                                    %% "circe-core"                       % circeVersion,
    "de.heikoseeberger"                           %% "akka-http-circe"                  % "1.33.0",

  )}

cancelable in Global := true
fork in run := true
connectInput in run := true