name := "stream-journal-to-kafka"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/stream-journal-to-kafka"))

startYear := Some(2018)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := crossScalaVersions.value.last

crossScalaVersions := Seq("2.11.12", "2.12.6")

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
  "com.typesafe.akka" %% "akka-persistence" % "2.5.14",
  "com.evolutiongaming" %% "executor-tools" % "1.0.1" % Test,
  "com.evolutiongaming" %% "skafka-api" % "1.2.0",
  "com.evolutiongaming" %% "future-helper" % "1.0.3",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true