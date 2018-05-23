import sbt.Keys.name

name := "codefeedr"
ThisBuild / version := "0.1-SNAPSHOT"
ThisBuild / organization := "org.codefeedr"
ThisBuild / scalaVersion := "2.11.11"

parallelExecution in Test := false

/// PROJECTS

val projectPrefix = "codefeedr-"

lazy val root = (project in file("."))
  .settings(settings)
  .aggregate(
    core,
    pluginRss,
    pluginMongodb,
    pluginElasticSearch,
    pluginGitHub,
    pluginHttpd
  )

lazy val core = (project in file("codefeedr-core"))
  .settings(
    name := "core",
    settings,
    unmanagedBase := baseDirectory.value / "../lib",
    libraryDependencies ++= commonDependencies ++ Seq(
      // JSONBuffer
      dependencies.json4s,
      dependencies.jackson,
      dependencies.json4sExt,

      // KafkaBuffer
      dependencies.kafkaClient,
      dependencies.flinkKafka,

      // RedisKeyManager
      dependencies.redis,

      // Schema exposure
      dependencies.zookeeper,

      "me.lyh" %% "shapeless-datatype-avro" % "0.1.9"
    )
  )

lazy val pluginRss = (project in file("codefeedr-plugins/codefeedr-rss"))
  .settings(
    name := "rss",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.httpj
    )
  )
  .dependsOn(
    core
  )

lazy val pluginMongodb = (project in file("codefeedr-plugins/codefeedr-mongodb"))
  .settings(
    name := "mongodb",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.mongo
    )
  )
  .dependsOn(
    core
  )

lazy val pluginElasticSearch = (project in file("codefeedr-plugins/codefeedr-elasticsearch"))
  .settings(
    name := "elasticsearch",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.flinkElasticSearch
    )
  )
  .dependsOn(
    core
  )

lazy val pluginGitHub = (project in file("codefeedr-plugins/codefeedr-github"))
  .settings(
    name := "github",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
      dependencies.httpj,
      dependencies.json4s,
      dependencies.jackson,
      dependencies.json4sExt
    )
  )
  .dependsOn(
    core
  )

lazy val pluginHttpd = (project in file("codefeedr-plugins/codefeedr-httpd"))
  .settings(
    name := "httpd",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
    )
  )
  .dependsOn(
    core
  )


lazy val dependencies =
  new {
    val flinkVersion    = "1.4.2"
    val avroVersion     = ""
    val json4sVersion   = "3.6.0-M2"

//    "org.apache.logging.log4j" % "log4j-api" % "2.11.0",
//    "org.apache.logging.log4j" % "log4j-core" % "2.11.0",

    val flink                     = "org.apache.flink"  %% "flink-scala"                            % flinkVersion    % "compile"
    val flinkStreaming            = "org.apache.flink"  %% "flink-streaming-scala"                  % flinkVersion    % "compile"
    val flinkKafka                = "org.apache.flink"  %% "flink-connector-kafka-0.11"             % flinkVersion    % "compile"
    val flinkAvro                 = "org.apache.flink"  %% "flink-avro"                             % flinkVersion
    val flinkRuntimeWeb           = "org.apache.flink"  %% "flink-runtime-web"                      % flinkVersion
    val flinkElasticSearch        = "org.apache.flink"  %% "flink-connector-elasticsearch5"         % flinkVersion

    val redis                     = "net.debasishg"     %% "redisclient"                            % "3.6"
    val kafkaClient               = "org.apache.kafka"   % "kafka-clients"                          % "1.0.0"
    val zookeeper                 = "org.apache.zookeeper" % "zookeeper"                            % "3.4.9"

    val json4s                    = "org.json4s"        %% "json4s-scalap"                          % json4sVersion
    val jackson                   = "org.json4s"        %% "json4s-jackson"                         % json4sVersion
    val json4sExt                 = "org.json4s"        %% "json4s-ext"                             % json4sVersion

    val mongo                     = "org.mongodb.scala" %% "mongo-scala-driver"                     % "2.3.0"

    val httpj                     = "org.scalaj"        %% "scalaj-http"                            % "2.4.0"

    val scalactic                 = "org.scalactic"     %% "scalactic"                              % "3.0.1"         % "test"
    val scalatest                 = "org.scalatest"     %% "scalatest"                              % "3.0.1"         % "test"
    val scalamock                 = "org.scalamock"     %% "scalamock"                              % "4.1.0"         % "test"
    val mockito                   = "org.mockito"        % "mockito-all"                            % "1.10.19"       % "test"
  }

lazy val commonDependencies = Seq(
  dependencies.flink,
  dependencies.flinkStreaming,

  // avro

  dependencies.scalactic,
  dependencies.scalatest,
  dependencies.scalamock,
  dependencies.mockito
)

// SETTINGS

lazy val settings = commonSettings

lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.11",

  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "confluent"                               at "http://packages.confluent.io/maven/",
    "Apache Development Snapshot Repository"  at "https://repository.apache.org/content/repositories/snapshots/",
    "Artima Maven Repository"                 at "http://repo.artima.com/releases",
    Resolver.mavenLocal
  )
)

lazy val compilerOptions = Seq(
  //  "-unchecked",
  //  "-feature",
  //  "-language:existentials",
  //  "-language:higherKinds",
  //  "-language:implicitConversions",
  //  "-language:postfixOps",
  //  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _                             => MergeStrategy.first
  }
)

//
//val coreDependencies = Seq(
//  "org.apache.zookeeper" % "zookeeper" % "3.4.9",
//  // "org.mockito" % "mockito-core" % "2.13.0" % "test",
//
//
//  "net.debasishg" %% "redisclient" % "3.6",
//
//  "org.mongodb.scala" %% "mongo-scala-driver" % "2.3.0",
//
//  "org.apache.logging.log4j" % "log4j-api" % "2.11.0",
//  "org.apache.logging.log4j" % "log4j-core" % "2.11.0",
//
//
//  "me.lyh" %% "shapeless-datatype-avro" % "0.1.9",
//
//  // https://mvnrepository.com/artifact/org.scalaj/scalaj-http
//  "com.chuusai" %% "shapeless" % "2.3.3",
//

//)

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

