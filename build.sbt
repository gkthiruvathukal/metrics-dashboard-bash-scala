name := "metrics-dashboard-bash-scala"

version := "1.0"

//scopt works with this version of scala
scalaVersion := "2.10.6"

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.apache.spark" % "spark-core_2.10" % "1.6.0" % "provided",
  "com.github.scopt" %% "scopt" % "3.4.0",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.6.3",
  "org.mongodb" %% "casbah" % "2.8.1",
  "io.spray" %% "spray-json" % "1.3.2",
  "com.squants"  %% "squants"  % "0.5.3"
)

mainClass in assembly := Some("edu.luc.cs.metrics.dashboard.Ingestion")
