The pipeline system is the core of the framework. It allows you to combine and pipeline multiple Flink jobs using a configurable buffer. 

## Stages
Each pipeline consists of multiple stages whereas each stage is a Flink job. There are three types of stages:

- InputStage: does not retrieve any input from a different stage but does write to 1 or more stages.
- TransformStage: retrieves from or 1 or more stages and writes to 1 or more stages.
- OutputStage: retrieves from 1 or more stages but does not write to any stages

Stages are linked to each other through [buffers](buffers). 
### InputStage
The InputStage will push data into your pipeline using a Flink source. To retrieve the Flink `StreamExecutionEnviroment` use `environment`.  

A simple example of an InputStage:
```scala
case class SimpleData(str: String)

class SimpleInputStage(stageAttributes: StageAttributes = StageAttributes()) 
                       extends InputStage[SimpleData](stageAttributes) {

  override def main(): DataStream[String] = {
    environment
      .fromCollection(Seq(SimpleData("Simple"), SimpleData("data"), SimpleData("set")))
  }
}
```
### TransformStage
In the TransformStage data is read from 1 or more stages, transformed using a Flink job and then outputted to the other stages. Currently up until 4 inputs are supported for a TransformStage (simply suffix it with #inputs, e.g. `TransformStage3`). 

A simple example of a TransformStage: 
```scala
case class SimpleData(str: String)
case class SimpleDataReduce(str: String, amount: Int)

class SimpleTransformStage(stageAttributes: StageAttributes = StageAttributes()) 
                       extends TransformStage[SimpleData, SimpleDataReduce](stageAttributes) {

  override def transform(input: DataStream[SimpleData): DataStream[SimpleDataReduce] = {
    input.map(x => (x.str, 1)
         .keyBy(0)
         .sum(1)
         .map(x => SimpleDataReduce(x._1, x._2))
  }
}
```
### OutputStage
In the OutputStage data is read from 1 or more stages and then transformed using a Flink job. Currently up until 4 inputs are supported for a OutputStage (simply suffix it with #inputs, e.g. `OutputStage3`). 

A simple example of an OutputStage:
```scala
case class SimpleDataReduce(str: String, amount: Int)

class SimpleOutputStage(stageAttributes: StageAttributes = StageAttributes()) 
                       extends OutputStage[SimpleDataReduce](stageAttributes) {

  override def main(input: DataStream[SimpleDataReduce): Unit = {
    input.print()
  }
}
```

### Create Your Own Stage
In order to create your own stage, consider the following:

- The stage should extend the correct base (either InputStage, TransformStage, OutputStage).
- If the stage needs to read from multiple other stages (and perform for instance a join), extend the base stage suffixed with the amount of stages it reads from; e.g. TransformStage3 to read from 3 sources. This is only applicable for the Transform and Output stage. Currently up until 4 input stages are supported. 
- Make sure the stage is **serializable**. This is a Flink requirement. 
- Overload `StageAttributes` to the stage constructor. **Note** this is only required if you need the StageAttributes for that particular stage.

Remember that stages run as independent Flink jobs with buffers (like Kafka), this creates overhead. Consider if your stage is worth this overhead instead of combining multiple stages into one stage. 

To have a better understanding of stages take a look at the plugins package which includes multiple (complex) stages.


## PipelineBuilder
The PipelineBuilder is used to create a pipeline of stages. Next to that, it allows to set the buffer type (like Kafka), buffer properties and key management.  A simple sequential pipeline can be created like this:
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

using Kafka as buffer in between. If you want to create more complex pipelines ([DAG's](https://en.wikipedia.org/wiki/Directed_acyclic_graph) are supported) make use of `edge` instead of `append`. The example above can be recreated using `edge` like this:
```scala
 val stage2 = new SimpleTransformStage()

 new PipelineBuilder()
   .setBufferType(BufferType.Kafka)
   .edge(new SimpleInputStage(), stage2)
   .edge(stage2, new SimpleOutputStage())
   .build()
```
This flow of this simple pipeline with Kafka as buffer is visualized as:
<p align="center"><img src="https://i.imgur.com/g4C3AB0.png" width="600"></p>
However the actual architecture of this pipeline can be seen in this figure:

<p align="center"><img src="https://i.imgur.com/9k4puOS.png" width="400"></p>

**Note:** Type-safety is **NOT** guaranteed in between stages. E.g. if Stage1 outputs type `A` and Stage2 reads from Stage1, Stage2 is not explicitly required to have `A` as input type. As long as the serialization framework will support the conversion (if you remove fields, this is often supported)  or the type is the same, it will not give problems. 

### Start the pipeline
If the Pipeline is properly build using the PipelineBuilder, it can be started in three modes:

- mock: creates one Flink `DataStream` of all the stages and runs it without buffer. Only works for **sequential** pipelines.
- local: start all stages in different threads.
- clustered: start each stage individually.


## Subjects
In order to let the stages interact with each other they all have a subject. By default this subject is equal to the name of the class. 
This subject is used to identify the name of the buffer and therefore this should be unique.
If you have stages with the same name which should **not** share the same buffer, you should override its subject by using the `StageAttributes` and passing it to the constructor of the Stage.
