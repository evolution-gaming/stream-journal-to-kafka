name := "stream-journal-to-kafka"

organization := "com.evolutiongaming"

homepage := Some(new URL("http://github.com/evolution-gaming/stream-journal-to-kafka"))

startYear := Some(2018)

organizationName := "Evolution Gaming"

organizationHomepage := Some(url("http://evolutiongaming.com"))

bintrayOrganization := Some("evolutiongaming")

scalaVersion := crossScalaVersions.value.head

crossScalaVersions := Seq("2.12.8")

resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-persistence"    % "2.5.23",
  "com.evolutiongaming"    %% "executor-tools"      % "1.0.1" % Test,
  "com.evolutiongaming"    %% "skafka"              % "4.0.4",
  "com.evolutiongaming"    %% "future-helper"       % "1.0.5",
  "org.scala-lang.modules" %% "scala-java8-compat"  % "0.9.0",
  "org.scalatest"          %% "scalatest"           % "3.0.8" % Test)

licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT")))

releaseCrossBuild := true