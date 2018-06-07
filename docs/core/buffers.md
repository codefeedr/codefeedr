Between each stage a buffer is added. These buffers are necessary to allow a directed acylic graph as pipeline where a plugin might read from multiple other stages (using this buffer). Currently [Kafka](https://kafka.apache.org/) and [RabbitMQ](https://www.rabbitmq.com/) are supported as buffers.

Buffers can be selected through the pipeline builder:
```scala
pipelineBuilder
  .setBufferType(BufferType.Kafka)
```

The default buffer is Kafka. 

## Configuration
Buffer properties can be set through the pipeline builder, which are just simple key/value combinations:
```scala 
pipelineBuilder
    .setBufferProperty("key", "value")
```
The buffers have some default values (like the Kafka broker), to override these obtain the key through `KafkaBuffer` or `RabbitMQBuffer`:
```scala 
pipelineBuilder
    .setBufferProperty(KafkaBuffer.BROKER, "localhost:9092")
```

**Note:** in case of the Kafka buffer all these properties will be propagated to the Kafka producer/consumer, see the [Kafka documentation](https://kafka.apache.org/documentation/#configuration) for specifics.
### Default properties

#### Kafka
|           Name          | Description                                                                                                                                   |  Default value |
|:-----------------------:|-----------------------------------------------------------------------------------------------------------------------------------------------|:--------------:|
| BROKER                  | The Kafka broker list.                                                                                                                        | localhost:9092 |
| ZOOKEEPER               | The Zookeeper host.                                                                                                                           | localhost:2181 |
| AUTO_OFFSET_RESET       | What to do when there is no initial offset in Kafka or if the current offset does not exist any more on the server: earliest, latest and none | earliest       |
| AUTO_COMMIT_INTERVAL_MS | The frequency in milliseconds that the consumer offsets are auto-committed to Kafka if enable.auto.commit is set to true.                     | 100            |
| ENABLE_AUTO_COMMIT      | If true the consumer's offset will be periodically committed in the background.                                                               | true           |

#### RabbitMQ
| Name | Description                  |     Default value     |
|:----:|------------------------------|:---------------------:|
| URI  | The RabbitMQ connection URI. | amqp://localhost:5672 |

## Serialization
Before data is send to a buffer it is serialized first (and deserialized if read out of the buffer). Currently we offer the following ways of serializing: 

- [JSON](https://www.json.org/)
- [Kryo](https://github.com/EsotericSoftware/kryo)
- [BSON](http://bsonspec.org/)

The serializer can be configured through the buffer properties: 

```scala
pipeline.setBufferProperty(Buffer.SERIALIZER, Serializer.KRYO)
```

The default serializer is JSON. 
We recommend to use the Kryo serializer since it has the best compression and speed. However, for debugging purposes you might want to use JSON since it offers readability.

## Kafka GroupID
If a stage reads from a buffer (transform/output stage), then the group id of its Kafka consumer is equal to the stage id.
To override this, set the `StageAttributes` and pass it to constructor of the stage. 

**Note** Don't set the `"group.id"` as buffer property because that property will be used throughout the whole pipeline. 
