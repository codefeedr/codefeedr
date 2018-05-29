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
 *
 */
package org.codefeedr.stages.kafka

import java.util.Properties

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.buffer.serialization.{AvroSerde, Serializer}
import org.codefeedr.pipeline.PipelineItem
import org.codefeedr.stages.{InputStage, StageAttributes}

import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.universe._

/**
  * KafkaInput stage, which reads from a Kafka topic.
  * @param topic the topic to read from.
  * @param properties kafka properties, see https://kafka.apache.org/documentation/#consumerconfigs
  * @param serializer the serializer to use for deserialization of the data, see [[Serializer]].
  * @param stageAttributes attributes of this stage.
  */
class KafkaInput[T <: PipelineItem : ClassTag : TypeTag : AvroSerde](topic: String,
                                                                     properties: Properties,
                                                                     serializer: String = Serializer.JSON,
                                                                     stageAttributes: StageAttributes = StageAttributes())
  extends InputStage[T] {
  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  //get correct serde, will fallback to JSON
  private val serde = Serializer.getSerde[T](serializer)

  //add flink kafka consumer
  override def main(): DataStream[T] = {
    pipeline.environment
      .addSource(new FlinkKafkaConsumer011[T](topic, serde, properties))
  }

}
