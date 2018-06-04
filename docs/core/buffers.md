Between each plugin (PipelineObject) a buffer is added. These buffers are necessary to allow a directed acylic graph as pipeline where a plugin might read from multiple other plugins (using this buffer). Currently only a [Kafka](https://kafka.apache.org/) buffer is supported.  
## KafkaBuffer
### Serialization
Before data is send to the Kafka buffer it is serialized. Currently we offer two ways of serializing: [JSON](https://www.json.org/) and [Avro](https://avro.apache.org/). This can be configured through the buffer properties: `pipeline.setBufferProperty(KafkaBuffer.SERIALIZER, Serializer.AVRO)`. The default serializer is JSON. 

We recommend to use the Avro serializer since the data is compressed and send in binary. However, for debugging purposes you might want to use JSON since it offers readability.

### Schema exposure
Kafka buffers offer functionality to expose the Avro schema of your data type to an external service. Currently we provide [redis](https://redis.io/) and [Zookeeper](https://zookeeper.apache.org/) to expose those schemas. If schema exposure is enabled each plugin will expose its type (in the form of an Avro schema) to the external service. The schemas are generated on run-time based on its (case) class. Schemas are stored as k/v pairs with the topic as key and value as schema. You can configure if you want to deserialize from the buffer using the exposed schema or infer a schema based on the (case) class defined. In the image below you can see a simple visualization of schema exposure including deserialization of the exposed schema.

<p align="center"><img src="https://i.imgur.com/QPI290F.png" width="600"></p>

**Note**: Schemas can be exposed using a JSON serializer, however you can't deserialize using the schema. 
### Configuration
In the Table below you can find the properties you can set for a Kafka buffer. Specific properties of a Kafka consumer/producer can be found in the [Flink](https://ci.apache.org/projects/flink/flink-docs-release-1.4/dev/connectors/kafka.html) and [Kafka](https://kafka.apache.org/documentation/#consumerconfigs) documentation.

|               Name              | Description                                                                              |       Possible values      |
|:-------------------------------:|------------------------------------------------------------------------------------------|:--------------------------:|
| BROKER                          | The kafka broker list.                                                                   |                            |
| ZOOKEEPER                       | The Zookeeper host.                                                                      |                            |
| SERIALIZER                      | The type of serializer to use for buffering data.                                        | AVRO, JSON                 |
| SCHEMA_EXPOSURE                 | Enable this to expose the (Avro) data schema of a buffer subject to an external service. | true, false                |
| SCHEMA_EXPOSURE_SERVICE         | The service to expose the (Avro) schemas.                                                | redis (default), zookeeper |
| SCHEMA_EXPOSURE_HOST            | The host of the service to expose the (Avro) schemas.                                    |                            |
| SCHEMA_EXPOSURE_DESERIALIZATION | Enable this to use the exposed (Avro) schema to deserialize data from a buffer.          | true, false                |

Example usage `pipeline.setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE_SERVICE, "zookeeper")`