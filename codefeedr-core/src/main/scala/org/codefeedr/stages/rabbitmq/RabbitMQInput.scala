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

package org.codefeedr.stages.rabbitmq

import java.net.URI

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.codefeedr.buffer.serialization.{AbstractSerde, Serializer}
import org.codefeedr.stages.{InputStage, StageAttributes}

import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}

/**
  * Input stage pulling data from a RabbitMQ queue.
  *
  * @param stageAttributes Optional attributes
  * @tparam T Type of value to pull from the queue
  */
class RabbitMQInput[T <: Serializable with AnyRef : ClassTag : TypeTag](queue: String,
                                                                        server: URI = new URI("amqp://localhost:5672"),
                                                                        stageAttributes: StageAttributes = StageAttributes())
  extends InputStage[T](stageAttributes) {

  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  override def main(): DataStream[T] = {
    val config = new RMQConnectionConfig.Builder()
      .setUri(server.toString)
      .build

    // Create a source with correlation id usage enabled for exactly once guarantees
    val source = new RMQSource[T](config, queue, true, getSerializer)

    pipeline.environment
      .addSource(source)
      .setParallelism(1) // Needed for exactly one guarantees
  }

  /**
    * The serializer to use for reading data from RabbitMQ.
    *
    * Override to use a different serialization than JSON.
    *
    * @return Serializer
    */
  protected def getSerializer: AbstractSerde[T] = {
    val serializer = Serializer.JSON

    Serializer.getSerde[T](serializer)
  }
}
