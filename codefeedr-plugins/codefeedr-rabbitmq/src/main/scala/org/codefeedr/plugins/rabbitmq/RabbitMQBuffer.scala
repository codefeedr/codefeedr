/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codefeedr.plugins.rabbitmq

import org.apache.flink.api.common.serialization.SerializationSchema
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.rabbitmq.{RMQSink, RMQSource}
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.codefeedr.buffer.Buffer
import org.codefeedr.pipeline.Pipeline

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object RabbitMQBuffer {

  /**
    * PROPERTIES
    */
  val URI = "URI"
}

/** Buffer using a RabbitMQ queue.
  *
  * @param pipeline The pipeline for which we use this Buffer.
  * @param properties The properties of this Buffer.
  * @tparam T Type of the data in this Buffer.
  */
class RabbitMQBuffer[T <: Serializable with AnyRef: ClassTag: TypeTag](
    pipeline: Pipeline,
    properties: org.codefeedr.Properties,
    relatedStageName: String)
    extends Buffer[T](pipeline, properties, relatedStageName) {

  /** Default settings for this RabbitMQ buffer. */
  private object RabbitMQBufferDefaults {
    val URI = "amqp://localhost:5672"
  }

  /** Get a RMQSource as source for a stage.
    *
    * @return The DataStream retrieved from the buffer.
    */
  override def getSource: DataStream[T] = {
    val connectionConfig = createConfig()

    // We use the related stage name for the queue.
    val queueName = relatedStageName

    // Create a source with correlation id usage enabled for exactly once guarantees.
    val source =
      new RMQSource[T](connectionConfig, queueName, true, getSerializer)

    pipeline.environment
      .addSource(source)
      .setParallelism(1) // Needed for exactly one guarantees
  }

  /** Get a RMQSink as sink to the buffer.
    *
    * @return The SinkFunction created by this RMQSink.
    */
  override def getSink: SinkFunction[T] = {
    val connectionConfig = createConfig()

    // We use the related stage name for the queue.
    val queueName = relatedStageName

    new RMQSinkDurable[T](connectionConfig, queueName, getSerializer)
  }

  /** Create a RabbitMQ configuration using buffer properties and defaults.
    *
    * @return A RabbitMQ configuration.
    */
  def createConfig(): RMQConnectionConfig = {
    new RMQConnectionConfig.Builder()
      .setUri(properties.getOrElse[String](RabbitMQBuffer.URI,
                                           RabbitMQBufferDefaults.URI))
      .build
  }
}

/** Setups a durable RabbitMQ Sink.
  *
  * @param rmqConnectionConfig The RabbitMQ connection configuration.
  * @param queueName The name of the RabbitMQ queue.
  * @param schema The serialization schema.
  * @tparam IN Type of data in the queue.
  */
class RMQSinkDurable[IN](rmqConnectionConfig: RMQConnectionConfig,
                         queueName: String,
                         schema: SerializationSchema[IN])
    extends RMQSink[IN](rmqConnectionConfig, queueName, schema) {

  /** Setups a new queue. */
  override def setupQueue(): Unit = {
    channel.queueDeclare(queueName, true, false, false, null)
  }

}
