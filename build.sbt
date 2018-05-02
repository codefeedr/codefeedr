// import sbt.Keys.libraryDependencies

resolvers in ThisBuild ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "CodeFeedr"
version := "0.1-SNAPSHOT"
organization := "org.codefeedr"

ThisBuild / scalaVersion := "2.11.11"

parallelExecution in Test := false

val flinkVersion = "1.4.2"
val flinkDependencies = Seq(
  "org.apache.flink" %% "flink-scala" % flinkVersion % "compile",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "compile",
  "org.apache.flink" %% "flink-connector-kafka-0.11" % flinkVersion % "compile"
//  "org.apache.flink" %% "flink-table" % flinkVersion % "provided"
)

val coreDependencies = Seq(
  // "codes.reactive" %% "scala-time" % "0.4.1",
  // "org.apache.zookeeper" % "zookeeper" % "3.4.9",
  // "org.mockito" % "mockito-core" % "2.13.0" % "test",
  // "org.json4s" % "json4s-scalap_2.11" % "3.6.0-M2",
  // "org.json4s" % "json4s-jackson_2.11" % "3.6.0-M2",


   "org.scalactic" %% "scalactic" % "3.0.5",
   "org.scalatest" %% "scalatest" % "3.0.5" % "test",

  // "ch.qos.logback" % "logback-classic" % "1.1.7",
  // "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

  // "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5" % "provided",
  // "com.typesafe" % "config" % "1.3.1",
  // "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",

  "org.apache.kafka" % "kafka-clients" % "1.0.0",
  // "com.jsuereth" %% "scala-arm" % "2.0",
  // "org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.8.0",
  // "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  // "io.reactivex" %% "rxscala" % "0.26.5"
)



lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= coreDependencies,
    libraryDependencies ++= flinkDependencies,
    libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }
  )

assembly / mainClass := Some("org.codefeedr.Main")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)

