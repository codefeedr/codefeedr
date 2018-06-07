## Setup
To use the framework import the following dependency in your project: 

## Example
This sections offers some examples for creating a simple pipeline. To create more complex pipelines, see the [pipeline section](#pipeline).
### WordCount
```scala
case class StringType(value: String) extends PipelineItem

class SimpleOutput extends OutputStage[StringType] {
  override def main(source: DataStream[StringType]): Unit = {
    source
      .map { item => (item.value, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }

}

object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new StringSource("Simple WordCount example example"))
      .append(new MyJob())
      .build()
      .start(args)
  }
}
```
To add Kafka as a buffer simple change the BufferType and add a Kafka broker:
```scala
pipelineBuilder
  .setBufferType(BufferType.Kafka)
  .setBufferProperty(KafkaBuffer.HOST, "localhost:9092")
```

## How To Run
Currently three running modes are supported.
- Mock: Each plugin is pipelined into **one** Flink job, no buffer is used.
    **Note**: A DAG pipeline is not supported in this mode.
- Local: Each plugin is run in one Flink execution environment, however a buffer is used.
- Cluster: TODO

It can be started from the code using `pipeline.runMock()`, `pipeline.runLocal()` or `pipeline.runClustered()`