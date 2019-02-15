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

package org.codefeedr.pipeline

import net.manub.embeddedkafka.{EmbeddedK, EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.buffer.BufferType
import org.codefeedr.stages.utilities.{StringInput, StringType}
import org.codefeedr.testUtils.CodeHitException
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class StageNTest extends FunSuite with BeforeAndAfterAll with EmbeddedKafka {

  override def beforeAll(): Unit = {
    implicit val config =
      EmbeddedKafkaConfig(zooKeeperPort = 2181, kafkaPort = 9092)
    EmbeddedKafka.start()
  }

  override def afterAll(): Unit = {
    EmbeddedKafka.stop()
  }

  class MyObject2 extends Stage2[StringType, StringType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[StringType]): DataStream[NoType] = {
      println("TRANSFORM", source, secondSource)

      if (source != null && secondSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  class MyBadObject2 extends Stage2[StringType, NoType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[NoType]): DataStream[NoType] = {
      if (source != null && secondSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  class MyObject3 extends Stage3[StringType, StringType, StringType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[StringType],
        thirdSource: DataStream[StringType]): DataStream[NoType] = {
      if (source != null && secondSource != null && thirdSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  class MyBadObject3 extends Stage3[StringType, StringType, NoType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[StringType],
        thirdSource: DataStream[NoType]): DataStream[NoType] = {
      if (source != null && secondSource != null && thirdSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  class MyObject4
      extends Stage4[StringType, StringType, StringType, StringType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[StringType],
        thirdSource: DataStream[StringType],
        fourthSource: DataStream[StringType]): DataStream[NoType] = {
      if (source != null && secondSource != null && thirdSource != null && fourthSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  class MyBadObject4
      extends Stage4[StringType, StringType, StringType, NoType, NoType] {
    override def transform(
        source: DataStream[StringType],
        secondSource: DataStream[StringType],
        thirdSource: DataStream[StringType],
        fourthSource: DataStream[NoType]): DataStream[NoType] = {
      if (source != null && secondSource != null && thirdSource != null && fourthSource != null) {
        throw CodeHitException()
      }

      null
    }
  }

  test("An object2 has two inputs") {
    val a = new StringInput()
    val b = new StringInput()
    val job = new MyObject2()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b)
      .build()

    assertThrows[CodeHitException] {
      pipeline.startLocal()
    }
  }

  test("An object3 has three inputs") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val job = new MyObject3()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c)
      .build()

    assertThrows[CodeHitException] {
      pipeline.startLocal()
    }
  }

  test("An object4 has four inputs") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val d = new StringInput()
    val job = new MyObject4()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c :+ d)
      .build()

    assertThrows[CodeHitException] {
      pipeline.startLocal()
    }
  }

  test("Should throw when not enough inputs are defined for an object2") {
    val a = new StringInput()
    val job = new MyObject2()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }

  test("Should throw when not enough inputs are defined for an object3") {
    val a = new StringInput()
    val b = new StringInput()
    val job = new MyObject3()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }

  test("Should throw when not enough inputs are defined for an object4") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val job = new MyObject4()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }

  test("Should not throw when plenty of inputs are defined for an object4") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val d = new StringInput()
    val e = new StringInput()
    val job = new MyObject4()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c :+ d :+ e)

    assertThrows[CodeHitException] {
      builder.build().startLocal()
    }
  }

  test("Should throw when input type of object2 is NoType") {
    val a = new StringInput()
    val b = new StringInput()
    val job = new MyBadObject2()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }

  test("Should throw when input type of object3 is NoType") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val job = new MyBadObject3()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }

  test("Should throw when input type of object4 is NoType") {
    val a = new StringInput()
    val b = new StringInput()
    val c = new StringInput()
    val d = new StringInput()
    val job = new MyBadObject4()

    val builder = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c :+ d)

    assertThrows[IllegalStateException] {
      builder.build()
    }
  }
}
