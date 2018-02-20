name := "stream-journal-to-kafka"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/stream-journal-to-kafka"))

startYear := Some(2018)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.12.4", "2.11.12")

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"
)

scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits", "-no-link-warnings")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % "2.5.9",
  "com.evolutiongaming" %% "executor-tools" % "1.0.0" % Test,
  "com.evolutiongaming" %% "skafka-api" % "0.2.6",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true