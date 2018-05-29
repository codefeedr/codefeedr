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
package org.codefeedr.buffer

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.buffer.serialization.{AbstractSerde, AvroSerde, Serializer}
import org.codefeedr.pipeline.Pipeline
import scala.reflect.runtime.universe._

import scala.reflect.ClassTag

/**
  * A pipeline buffer.
  *
  * A buffer is an often external source that stores and queues elements.
  *
  * @param pipeline Pipeline
  * @param properties Buffer properties
  * @tparam T Element type of the buffer
  */
abstract class Buffer[T <: AnyRef : ClassTag : TypeTag : AvroSerde](pipeline: Pipeline, properties: org.codefeedr.Properties) {

  /**
    * Get the source for this buffer. The buffer ereads from this
    *
    * @return Source stream
    */
  def getSource: DataStream[T]

  /**
    * Get the sink function for the buffer. The buffer writes to this.
    *
    * @return Sink function
    */
  def getSink: SinkFunction[T]

  /**
    * Get serializer/deserializer of elements
    *
    * @return Serializer
    */
  def getSerializer: AbstractSerde[T] = {
    val serializer = properties.getOrElse[String](Buffer.SERIALIZER, Serializer.JSON)

    Serializer.getSerde[T](serializer)
  }
}

/**
  * Buffer static values
  */
object Buffer {
  val SERIALIZER = "SERIALIZER"
}