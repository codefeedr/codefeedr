---
title: Buffers
tags: [internals, architecture]
keywords: internals, architecure, buffer
permalink: mydoc_buffer.html
sidebar: mydoc_sidebar
folder: mydoc
---

{% include important.html content="This page is under construction!" %}

Between each stage a buffer is added. These buffers are necessary to
allow a directed acylic graph as pipeline where a plugin might read from
multiple other stages (using this buffer). Currently only
[Kafka](https://kafka.apache.org/) is supported as buffer.

Buffers can be selected through the pipeline builder:
```scala
pipelineBuilder
  .setBufferType(BufferType.Kafka)
```

The default buffer is Kafka.

## Configuration
Buffer properties can be set through the pipeline builder, which are
just simple key/value combinations:
```scala
pipelineBuilder
    .setBufferProperty("key", "value")
```
The buffers have some default values (like the Kafka broker), to
override these values obtain the key through `KafkaBuffer` object:
```scala
pipelineBuilder
    .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
```

**Note:** in case of the Kafka buffer all these properties will be
propagated to the Kafka producer/consumer, see the [Kafka
documentation](https://kafka.apache.org/documentation/#configuration)
for specifics.
### Default properties

#### Kafka

|           Name          | Description                                                                                                                                   |  Default value |
|:-----------------------:|-----------------------------------------------------------------------------------------------------------------------------------------------|:--------------:|
| BROKER                  | The Kafka broker list.                                                                                                                        | localhost:9092 |
| ZOOKEEPER               | The Zookeeper host.                                                                                                                           | localhost:2181 |
| AUTO_OFFSET_RESET       | What to do when there is no initial offset in Kafka or if the current offset does not exist any more on the server: earliest, latest and none | earliest       |
| AUTO_COMMIT_INTERVAL_MS | The frequency in milliseconds that the consumer offsets are auto-committed to Kafka if enable.auto.commit is set to true.                     | 100            |
| ENABLE_AUTO_COMMIT      | If true the consumer's offset will be periodically committed in the background.                                                               | true           |

### Write Your Own Buffer
Instead of using Kafka, you can write and use your own buffer. To do so,
follow these steps:

1.
Write a buffer class which extends `Buffer[T]`:
```scala
class MyOwnBuffer[T <: Serializable with AnyRef: ClassTag: TypeTag](
    pipeline: Pipeline,
    properties: Properties,
    relatedStageName: String)
    extends Buffer[T](pipeline, properties, relatedStageName) {

  override def getSource: DataStream[T] = ??? // Your implementation here.
  override def getSink: SinkFunction[T] = ??? // Your implementation here.
}
```
Make sure to propagate the correct context bounds and parameters.

2.
Register the buffer before using it in a pipeline:
```scala
import org.codefeedr.api._

registerBuffer[MyOwnBuffer[_]]("name_of_buffer")
```

3.
Select the buffer in the pipeline builder:
```scala
pipeline.setBufferType("name_of_buffer")
```

## Serialization
Before data is send to a buffer it is serialized first (and deserialized
if read from the buffer). Currently we offer the following ways of
serializing:  

- [JSON](https://www.json.org/)
- [Kryo](https://github.com/EsotericSoftware/kryo)
- [BSON](http://bsonspec.org/)

The serializer can be configured through the
[PipelineBuilder](mydoc_pipeline.html#pipelinebuilder).

```scala
pipeline.setSerializer(Serializer.KRYO)
```

The default serializer is JSON.
We recommend to use the Kryo serializer since it has the best
compression and speed. However, for debugging purposes you might want to
use JSON since it offers readability.

### Write Your Own Serializer

It is possible to write your own serializer and register it in the
pipeline builder:

1.
Write a serializer class which extends `AbstractSerde`:
```scala
class MyOwnSerde[T <: Serializable with AnyRef: TypeTag: ClassTag] extends AbstractSerde[T] {   
  override def serialize(element: T): Array[Byte] = ??? // Your implementation here.
  override def deserialize(message: Array[Byte]): T = ??? // Your implementation here.
}
```
Make sure to add all the correct context bounds (Serializable with
AnyRef, TypeTag and ClassTag).

2.
Register your SerDe **before** building a pipeline:
```scala
import org.codefeedr.api._

registerSerializer[MyOwnSerde[_]]("name_of_serde")
```
Some names are already reserved (`kryo`, `json`, `bson`).

3.
Select the serde in the pipeline builder:
```scala
pipeline.setSerializer("name_of_serde")
```

## Kafka Group Id/Name
If a stage reads from a buffer (transform/output stage), then the group
id of its Kafka consumer is equal to the stage id (to ensure a stage
reads from its last offset). To override this, see
[here](mydoc_pipeline.html#stage-id). For more information on Kafka
consumer groups, see
[here](https://dzone.com/articles/understanding-kafka-consumer-groups-and-consumer-l).

**Note** Don't set the `"group.id"` as buffer property because that
property will be used throughout the whole pipeline.  
