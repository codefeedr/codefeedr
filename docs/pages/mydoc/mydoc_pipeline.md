---
title: Pipeline
tags: [internals, architecture]
keywords: internals, architecure, pipeline
permalink: mydoc_pipeline.html
sidebar: mydoc_sidebar
folder: mydoc
---
{% include important.html content="This page is under construction!" %}

The pipeline system is the core of the framework. It allows you to
combine and pipeline multiple Flink jobs using a configurable buffer.
It is important to understand how Flink DataStream API works in order to
write your own CodeFeedr pipeline. See the [Flink
documentation](https://flink.apache.org/).  

## Stages
Each pipeline consists of multiple stages whereas each stage is a Flink
job. There are three types of stages:

- InputStage: does not retrieve any input from a different stage but
does write to one or more stages (one-to-many).
- TransformStage: retrieves from or one or more stages and writes to one
or more stages (many-to-many).
- OutputStage: retrieves from one or more stages but does not write to
any stages (many-to-one).

Stages are linked to each other through [buffers](buffers), i.e. data
flows from one stage to another using a buffer.  

**Note**: When you're writing Flink code, make sure to import their
TypeInformation implicits: `import org.apache.flink.api.scala._`

### InputStage
The InputStage will push data into your pipeline using a Flink source.
To retrieve the Flink `StreamExecutionEnviroment` use the `Context`. The
[stage id](#stage-id) can be passed through the constructor, if none is
given the class name will be used.   

A simple example of an InputStage:
```scala
case class SimpleData(str: String)

class SimpleInputStage(stageId: Option[String] = None)
    extends InputStage[SimpleData](stageId) {

  override def main(context: Context) = {
    context.env
      .fromCollection(Seq(SimpleData("a"), SimpleData("b"), SimpleData("c")))
  }
}
```
### TransformStage
In the TransformStage data is read from 1 or more stages, transformed
using a Flink job and then outputted to the other stages. Currently up
until 4 inputs are supported for a TransformStage (simply suffix it with
#inputs, e.g. `TransformStage3`).  

A simple example of a TransformStage:
```scala
case class SimpleDataReduce(str: String, amount: Int)

class SimpleTransformStage(stageId: Option[String] = None)
    extends TransformStage[SimpleData, SimpleDataReduce](stageId) {

  override def transform(
      source: DataStream[SimpleData]): DataStream[SimpleDataReduce] = {
    source
      .map(x => (x.str, 1))
      .keyBy(0)
      .sum(1)
      .map(x => SimpleDataReduce(x._1, x._2))
  }
}

```
**Notice** how the `SimpleData` case class is re-used from the
SimpleInputStage.

### OutputStage
In the OutputStage data is read from 1 or more stages and then
transformed using a Flink job. Currently up until 4 inputs are supported
for a OutputStage (simply suffix it with #inputs, e.g. `OutputStage3`).

A simple example of an OutputStage:
```scala
class SimpleOutputStage(stageId: Option[String] = None)
    extends OutputStage[SimpleDataReduce](stageId) {

  override def main(source: DataStream[SimpleDataReduce]): Unit = source.print()
}
```
**Notice** how the `SimpleDataReduce` case class is re-used from the
SimpleTransformStage.

### Create Your Own Stage
In order to create your own stage, consider the following:

- The stage should extend the correct base (either InputStage,
TransformStage, OutputStage).
- If the stage needs to read from multiple other stages (and perform for
instance a join), extend the base stage suffixed with the amount of
stages it reads from; e.g. TransformStage3 to read from 3 sources. This
is only applicable for the Transform and Output stage. Currently up
until 4 input stages are supported.  
- Make sure the stage is **serializable** i.e. every element of your
stage should be seralizable. This is a Flink requirement. To bypass this
you can use [lazy
evaluation](https://stackoverflow.com/questions/9449474/def-vs-val-vs-lazy-val-evaluation-in-scala).
- Overload the `stageId` to the stage constructor. **Note** this is
only required if you need the stage to have a custom name (default is
the name of the class). For instance if you re-use the same stage, names
will conflict and you need to provide a custom name.  

Remember that stages run as independent Flink jobs with buffers (like
Kafka), this creates overhead. Consider if your stage is worth this
overhead instead of combining multiple stages into one stage.  

To have a better understanding of stages take a look at the plugins
package which includes multiple (complex) stages.


## PipelineBuilder
The PipelineBuilder is used to create a pipeline of stages. Next to
that, it allows to set the buffer type (like Kafka), buffer properties
and key management.  A simple sequential pipeline can be created like
this:
```scala
 new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(new SimpleInputStage())
      .append(new SimpleTransformStage())
      .append(new SimpleOutputStage())
      .build()
```
This will create and connect the stages like this:

`SimpleInputStage -> SimpleTransformStage -> SimpleOutputStage`

using Kafka as buffer in between. If you want to create more complex
pipelines ([DAG's](https://en.wikipedia.org/wiki/Directed_acyclic_graph)
are supported) make use of `edge` instead of `append`. The example above
can be recreated using `edge` like this:
```scala
 val stage2 = new SimpleTransformStage()

 new PipelineBuilder()
   .setBufferType(BufferType.Kafka)
   .edge(new SimpleInputStage(), stage2)
   .edge(stage2, new SimpleOutputStage())
   .build()
```
This flow of this simple pipeline with Kafka as buffer is visualized as:
<p align="center"><img src="images/codefeedr_buffer.png"
width="600"></p> However the actual architecture of this pipeline can be
seen in this figure:

<p align="center"><img src="images/flink_kafka_cluster.png"
style="width: 500px"></p>

**Note:** Type-safety is guaranteed in between stages. E.g. if
Stage1 outputs type `A` and Stage2 reads from Stage1, Stage2 is
explicitly required to have `A` as input type. An error will be thrown
if this is not satisfied. This can be disabled in the pipeline builder
by `builder.disablePipelineVerification()`, however we do not recommend
this. If you disable this, make sure the serialization framework will
support the conversion (if you remove fields, this is often supported).

### Stage Properties
In the PipelineBuilder properties can be specified **per** stage. This
properties map is available to the Stage at run-time. To set a stage
property; the stage, a key and a value need to be specified. E.g.:

```scala
val stage = new SimpleInputStage()

new PipelineBuilder()
  .append(stage)
  .setStageProperty(stage, "the_key", "the_value")
  ...
```

Now within the stage a property can be retrieved like this:
```scala
getContext.getStageProperties("the_key")
```
This will return `Some("the_value")`, if the key is unknown `None` will
be returned.

### Start the pipeline
If the Pipeline is properly build using the PipelineBuilder, it can be
started in three modes:

- mock: creates one Flink `DataStream` of all the stages and runs it
without buffer. Only works for **sequential** pipelines.
- local: start all stages as separate Flink
applications.
- clustered: start each stage individually.


## Stage id
In order to let the stages interact with each other they all have a
stage id. By default this is equal to the name of the class.
This id is used to identify the name of the buffer and therefore
this should be unique. If you have stages with the same name which
should **not** share the same buffer, you should override its stage id
by passing it in the constructor of the stage.
