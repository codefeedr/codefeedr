---
title: "Create Your Own Plugin"
keywords: plugins, template
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_create_your_own_plugin.html
---

{% include warning.html content="This page is outdated!" %}
## Setup
To use the framework import the following dependency in your project:

## Template
If you want to start from scratch, you can download our template using
[Giter8](http://www.foundweekends.org/giter8/):

`sbt new Jorisq/bep_codefeedr-project.g8`

This template contains a simple WordCount example, which is discussed
below.
## Example
This sections offers a WordCount example also available in the
[template](#template). For more in-depth explanation on how to create
stages for a pipeline, see the [pipeline](core/pipeline) section.
### WordCount
First of all an OutputStage is created, this is a stage which reads from
a buffer but doesn't write to one. In this OutputStage a simple
WordCount is executed using the Flink DataStream API.
```scala
class WordCountOutput extends OutputStage[StringType] {
  override def main(source: DataStream[StringType]): Unit = {
    source
      .map { item => (item.value, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }
}
```
The `StringType` is a simple case class already defined in our
framework:

```scala
case class StringType(value: String)
```

To run this stage, it needs to be added to a pipeline. You can do this
using the `PipelineBuilder`, in a sequential pipeline you simple use the
`append` method to add new stages to the pipeline. Finally the pipeline
is `build` and then started.

```scala
object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new StringInput("Hello\n" +
        "World!\n" +
        "How\n" +
        "are\n" +
        "you\n" +
        "doing?"))
      .append (new WordCountOutput)
      .build()
      .start(args)
  }
}
```
The StringInput is a stage pre-defined in the CodeFeedr framework, it
simple converts a string into words and parses it to the `StringType`
case class. By default a Kafka buffer is used. Starting this pipeline
will result in the following data flow: <p align="center"><img
src="https://i.imgur.com/LOGmdK2.png" width="400"></p>

## How To Run
Currently three running modes are supported.

- Mock: Each stage is pipelined into **one** Flink job, no buffer is
used. <br> **Note**: A DAG pipeline is not supported in this mode.
- Local: Each stage is run in one Flink execution environment, however a
buffer is used.
- Cluster: A stage is run individually, you have to specify which stage.

The pipeline can be started from the code using `pipeline.runMock()`,
`pipeline.runLocal()`, `pipeline.runClustered()` or via start arguments
using: `pipeline.start(args)`.
