name := "akka-bookstore"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging"  %%  "scala-logging"     % "3.9.2",
  "ch.qos.logback"              %   "logback-core"      % "1.2.3",
  "ch.qos.logback"              %   "logback-classic"   % "1.2.3",
  "io.spray"                    %%  "spray-json"        % "1.3.5"
)

// Akka dependencies
val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-stream"
).map(_ % akkaVersion)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http",
  "com.typesafe.akka" %% "akka-http-spray-json"
).map(_ % akkaHttpVersion)

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.1.0"