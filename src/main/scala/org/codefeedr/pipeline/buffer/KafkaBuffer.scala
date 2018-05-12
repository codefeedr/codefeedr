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
package org.codefeedr.pipeline.buffer

import java.util.Properties

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.buffer.serialization._

import scala.reflect.classTag
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline

import scala.reflect.Manifest

object KafkaBuffer {
  val BROKER = "HOST"
  val ZOOKEEPER = "ZOOKEEPER"
  val SERIALIZER = "SERIALIZER"

  val DEFAULT_BROKER = "localhost:9092"
  val DEFAULT_ZOOKEEPER = "localhost:2181"
}

class KafkaBuffer[T <: AnyRef : Manifest : FromRecord](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {

  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  override def getSource: DataStream[T] = {
    val props = pipeline.bufferProperties

    val kafkaProp = new java.util.Properties()
    kafkaProp.put("bootstrap.servers", props.get[String](KafkaBuffer.BROKER).
      getOrElse(KafkaBuffer.DEFAULT_BROKER))
    kafkaProp.put("zookeeper.connect", props.get[String](KafkaBuffer.ZOOKEEPER).
      getOrElse(KafkaBuffer.DEFAULT_ZOOKEEPER))
    kafkaProp.put("auto.offset.reset", "earliest")
    kafkaProp.put("auto.commit.interval.ms", "100")
    kafkaProp.put("enable.auto.commit", "true")

    val serde = Serializer.
      getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).
      getOrElse(Serializer.JSON))

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, serde, kafkaProp))
  }

  override def getSink: SinkFunction[T] = {
    val props = pipeline.bufferProperties

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).
      getOrElse(Serializer.JSON))

    val kafkaProp = new java.util.Properties()
    kafkaProp.put("bootstrap.servers", props.get[String](KafkaBuffer.BROKER).
      getOrElse(KafkaBuffer.DEFAULT_BROKER))
    kafkaProp.put("zookeeper.connect", props.get[String](KafkaBuffer.ZOOKEEPER).
      getOrElse(KafkaBuffer.DEFAULT_ZOOKEEPER))
    kafkaProp.put("auto.offset.reset", "earliest")
    kafkaProp.put("auto.commit.interval.ms", "100")
    kafkaProp.put("enable.auto.commit", "true")

    new FlinkKafkaProducer011[T](topic, serde, kafkaProp)
  }
}