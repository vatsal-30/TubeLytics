name := """SOEN-6441-TubeLytics"""
organization := "concordia.soen_6441"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

lazy val akkaVersion = "2.9.5"
lazy val akkaHttpVersion = "10.5.3"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
fork := true
fork in Test := false
libraryDependencies += guice

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.play" %% "play-akka-http-server" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % "2.9.5" % Test,
  javaWs,
//  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "junit" % "junit" % "4.13.1" % Test,
  "org.mockito" % "mockito-core" % "5.12.0" % Test,
//  "org.mockito" % "mockito-inline" % "5.12.0" % Test,
  "com.typesafe.play" %% "play-test" % "2.8.20" % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
//  "com.typesafe" % "config" % "1.4.x"

//  "org.awaitility" % "awaitility" % "4.2.0" % Test,
//  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2" % Test
)
//libraryDependencies += "org.mockito" %% "mockito-inline" % "4.0.0" % Test
libraryDependencies += "net.bytebuddy" % "byte-buddy" % "1.14.16" // or the latest available



libraryDependencies := libraryDependencies.value.map(_.excludeAll(
  ExclusionRule("org.apache.pekko"),
  ExclusionRule("org.playframework", "play-pekko-http-server_2.13")
))


enablePlugins(JacocoPlugin)
jacocoExcludes := Seq("controllers.HomeController", "views.*", "router", "controllers.javascript")

PlayKeys.devSettings += "play.server.http.idleTimeout" -> "infinite"
PlayKeys.devSettings += "play.client.http.idleTimeout" -> "infinite"