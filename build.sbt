name := "twitterapiv2"
scalaVersion := "2.13.4"
version := "0.1"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(

  "com.github.alexarchambault" %% "case-app" % "2.0.1",
  "com.github.pureconfig" %% "pureconfig" % "0.14.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.lihaoyi" %% "utest" % "0.7.2" % "test",
  "com.lihaoyi" %% "os-lib" % "0.7.3",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "io.spray" %%  "spray-json" % "1.3.6",
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.lihaoyi" %% "requests" % "0.7.0")

  testFrameworks += new TestFramework("utest.runner.Framework")

mainClass in (Compile, run) := Some("dev.habla.twitter.v2.main.Main")
mainClass in assembly := Some("dev.habla.twitter.v2.main.Main")
assemblyJarName in assembly := "twitterv2-" + version.value + ".jar"


