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
package org.codefeedr.buffer

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.Properties
import org.codefeedr.pipeline.{Pipeline, PipelineBuilder}
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.{SimpleSourceStage, SimpleTransformStage}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.codefeedr.api._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class BufferFactoryTest extends FunSuite with BeforeAndAfter {

  val nodeA = new SimpleSourceStage()
  val nodeB = new SimpleTransformStage()

  test("Should throw when giving a null object") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, null)

    assertThrows[IllegalArgumentException] {
      factory.create[StringType]()
    }
  }

  test("Should give a configured Kafka buffer when buffertype is kafka") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, nodeB)

    // created for nodeB sink, so should have subject of nodeB
    val nodeSubject = nodeB.getContext.stageId
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[KafkaBuffer[StringType]])
  }

  test("Register your own buffer.") {
    BufferFactory.register[DummyBuffer[_]]("my_buffer!")

    val pipeline = new PipelineBuilder()
      .setBufferType("my_buffer!")
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, nodeB)
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[DummyBuffer[StringType]])
  }

  test("Default fallback should be Kafka") {
    val pipeline = new PipelineBuilder()
      .setBufferType("doesn't exist!!!!")
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, nodeB)
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[KafkaBuffer[StringType]])
  }

  test("Cannot set Buffer with reserved keyword.") {
    assertThrows[IllegalArgumentException] {
      BufferFactory.register[DummyBuffer[_]](BufferType.Kafka)
    }
  }

  test("Cannot set Buffer with name already registered.") {
    BufferFactory.register[DummyBuffer[_]]("unique_name")
    assertThrows[IllegalArgumentException] {
      BufferFactory.register[DummyBuffer[_]]("unique_name")
    }
  }

  test("Register through codefeedr entry point.") {
    registerBuffer[DummyBuffer[_]]("my_bufferr!")

    val pipeline = new PipelineBuilder()
      .setBufferType("my_bufferr!")
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeA, nodeB)
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[DummyBuffer[StringType]])
  }

}

class DummyBuffer[T <: Serializable with AnyRef: ClassTag: TypeTag](
    pipeline: Pipeline,
    properties: Properties,
    stageName: String)
    extends Buffer[T](pipeline, properties, stageName) {

  override def getSource: DataStream[T] = null
  override def getSink: SinkFunction[T] = null
}
