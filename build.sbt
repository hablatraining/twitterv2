name := "twitterapiv2"
scalaVersion := "3.0.0"
version := "0.1"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"

libraryDependencies ++= Seq(
  ("com.github.alexarchambault" %% "case-app" % "2.0.6").cross(CrossVersion.for3Use2_13),
  ("com.github.pureconfig" %% "pureconfig" % "0.14.0").cross(CrossVersion.for3Use2_13),
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.lihaoyi" %% "utest" % "0.7.10" % "test",
  "com.lihaoyi" %% "os-lib" % "0.7.7",
  "org.typelevel" %% "cats-core" % "2.6.1",
  ("io.spray" %%  "spray-json" % "1.3.6").cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-slf4j" % AkkaVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-stream" % AkkaVersion).cross(CrossVersion.for3Use2_13),
  ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion).cross(CrossVersion.for3Use2_13))

testFrameworks += new TestFramework("utest.runner.Framework")

scalacOptions ++= {
  Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions"
  )}

mainClass in (Compile, run) := Some("dev.habla.twitter.v2.Main")
mainClass in assembly := Some("dev.habla.twitter.v2.Main")
assemblyJarName in assembly := "twitterv2-" + version.value + ".jar"


