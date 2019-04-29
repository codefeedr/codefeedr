---
title: "PyPi"
keywords: plugins, pypi
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_pypi.html
---
{% include tip.html content="We offer access to PyPi data in CodeFeedr as a service. See this page for more information." %}

The PyPi plugin is mainly focused on tracking new releases from [PyPi](https://pypi.org).
With the help of the PyPi plugin you can either hook into our CodeFeedr service or track PyPi releases yourself.

## Installation
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12)
```scala
dependencies += "org.codefeedr" %% "codefeedr-plugin-pypi" % "LATEST_VERSION"
``` 


## Stages
This plugin focuses on tracking new PyPi releases. Therefore there are two stages available:

### PyPiReleasesStage
The `PyPiReleasesStage` follows and parses the [release RSS feed](https://pypi.org/rss/updates.xml) provided by PyPi. Each [release](/mydoc_pypispec.html#release) is parsed as:
```scala
case class PyPiRelease(title: String,
                     link: String,
                     description: String,
                     pubDate: Date)
```
By default the data is pushed into the `pypi_releases_min` Kafka topic. Furthermore, the RSS feed is polled every second and the source runs indefinitely. 
Both properties can be configured through the `PyPiSourceConfig`:

```scala
/** Poll every 2 seconds only 10 times. */
val config = PyPiSourceConfig(2000, 10)

/** We override the default Kafka topic. */
val topic = "pypi"

/** We pass configuration by constructing the PyPiReleasesStage. */
val stage = new PyPiReleasesStage(topic, config)
```

### PyPiReleasesExtStage
The `PyPiReleasesExtStage` (asynchronously) enriches a `PyPiRelease` with all the project information. The full specification can be found [here](/mydoc_pypispec.html#release-extended). 
By default the data is pushed into the `pypi_releases` Kafka topic. **Note:** this stage relies on the [PyPiReleasesStage](#pypireleasesstage) stage to retrieve the (minimized) releases:

```scala
val releaseSource = new PyPiReleasesStage()
val enrichReleases = new PyPiReleasesExtStage()

new PipelineBuilder()
    .setBufferProperty("message.max.bytes", "5000000") // max message size is 5mb
    .setBufferProperty("max.request.size", "5000000") // max message size is 5 mb
    .edge(releaseSource, enrichReleases)
    .start(args)
```
## Notes
The maximum message in Kafka is by default set to 1MB. Some events might be bigger than 1MB and this will crash the plugin. 
To fix this you have to increase the Kafka message size in the pipeline as well as in your broker.
Add the following lines when building your pipeline:
```scala
builder
    .setBufferProperty("message.max.bytes", "5000000") // max message size is 5mb
    .setBufferProperty("max.request.size", "5000000") // max message size is 5 mb
```

Finally, configure your Kafka broker(s) with the following properties:
```yml
message.max.bytes: 5000000
replica.fetch.max.bytes: 5000000
```