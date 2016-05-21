import sbt.Keys._

name := "Device Data Capture"

organization in ThisBuild := "com.iot.series"
version      in ThisBuild := "1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val AkkaVersion                     = "2.4.2"
  val GoogleProtoBufVersion           = "2.6.1"
  val LogbackVersion                  = "1.1.5"
  val ScalaTestVersion                = "2.2.6"
  val ScalaLoggingVersion             = "3.1.0"
  val NettyVersion                    = "3.10.3.Final"
  val NettyHandlerVersion             = "4.0.34.Final"
  val MockitoVersion                  = "1.10.19"
  val MoquetteVersion                 = "0.8.1"
  val PahoClientVersion               = "1.0.2"
  val PahoVersion                     = "1.2.0-CHARIOT-FORK-REV-1"

  Seq(
    "ch.qos.logback"                              % "logback-classic"                   % LogbackVersion,
    "com.typesafe.akka"                          %% "akka-actor"                          % AkkaVersion
      exclude("io.netty", "netty"),
    "com.typesafe.akka"                          %% "akka-slf4j"                          % AkkaVersion,
    "com.typesafe.akka"                          %% "akka-remote"                         % AkkaVersion
      exclude ("io.netty", "netty"),
    "com.typesafe.scala-logging"                 %% "scala-logging"                       % ScalaLoggingVersion,
    "io.netty"                                    % "netty"                               % NettyVersion,
    "io.netty"                                    % "netty-handler"                       % NettyHandlerVersion,
    "org.eclipse.paho"                            % "org.eclipse.paho.client.mqttv3"      % PahoClientVersion,

    "com.typesafe.akka"                          %% "akka-testkit"                        % AkkaVersion                    % "test, provided",
    "io.moquette"                                 % "moquette-broker"                     % MoquetteVersion                % "test, provided",
    "org.mockito"                                 % "mockito-core"                        % MockitoVersion                 % "test, provided",
    "org.scalatest"                              %% "scalatest"                           % ScalaTestVersion               % "test, provided"
  )
}

resolvers in ThisBuild ++=
  Seq(
    Resolver.typesafeRepo("releases"),
    Resolver.bintrayRepo("websudos", "oss-releases"),
    "krasserm at bintray"              at "http://dl.bintray.com/krasserm/maven",
    "andsel at bintray"                at "http://dl.bintray.com/andsel/maven/",
    "Sonatype repo"                    at "https://oss.sonatype.org/content/groups/scala-tools/",
    "Sonatype releases"                at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype staging"                 at "http://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository"       at "http://download.java.net/maven/2/"
  )

conflictWarning in ThisBuild := ConflictWarning.disable

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-language:postfixOps",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

fork in run := true

parallelExecution in Test := false

parallelExecution in IntegrationTest := false

