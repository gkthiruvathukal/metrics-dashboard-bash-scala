name := "metrics-dashboard-bash-scala"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % Test,
  "org.apache.spark" % "spark-core_2.10" % "1.6.0" % "provided",
  "com.github.scopt" %% "scopt" % "3.4.0",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % "2.6.3"
)

mainClass in assembly := Some("edu.luc.cs.metrics.dashboard.Ingestion")
