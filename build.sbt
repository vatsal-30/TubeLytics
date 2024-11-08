name := """SOEN-6441-TubeLytics"""
organization := "concordia.soen_6441"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.15"

lazy val akkaVersion = "2.9.2"
lazy val akkaHttpVersion = "10.5.3"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")
fork := true
fork in Test := false
libraryDependencies += guice

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "junit" % "junit" % "4.13.1" % Test,
  "org.mockito" % "mockito-core" % "5.12.0" % Test)

libraryDependencies ++= Seq(
  javaWs
)

enablePlugins(JacocoPlugin)
jacocoExcludes := Seq("controllers.HomeController")
