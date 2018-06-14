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

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.buffer.{Buffer, BufferType, KafkaBuffer}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.flink.api.scala._
import org.apache.flink.runtime.client.JobExecutionException
import org.codefeedr.buffer.serialization.Serializer
import org.codefeedr.buffer.serialization.schema_exposure.{RedisSchemaExposer, ZookeeperSchemaExposer}
import org.codefeedr.stages.utilities.{JsonPrinterOutput, StringInput, StringType}
import org.codefeedr.testUtils._

import scala.collection.JavaConverters._

class PipelineTest extends FunSuite with BeforeAndAfter {

  var builder: PipelineBuilder = _

  before {
    builder = new PipelineBuilder()
    CollectSink.result.clear()
  }

  test("Simple pipeline test wordcount") {
    builder
      .append(new StringInput(str = "hallo hallo doei doei doei"))
      .append { x: DataStream[StringType] =>
        x.map(x => (x.value, 1))
          .keyBy(0)
          .sum(1)
          .map(x => WordCount(x._1, x._2))
      }
      .append { x: DataStream[WordCount] =>
        x.addSink(new CollectSink)
      }
      .build()
      .startMock()

    val res = CollectSink.result.asScala

    assert(res.contains(WordCount("doei", 3)))
    assert(res.contains(WordCount("hallo", 2)))
  }

  test("Simple pipeline test") {
    builder
      .append(new StringInput())
      .append { x: DataStream[StringType] =>
        x.map(x => WordCount(x.value, 1)).setParallelism(1)
          .addSink(new CollectSink).setParallelism(1)
      }
      .build()
      .startMock()

    val res = CollectSink.result.asScala

    assert(res.contains(WordCount("", 1)))
  }

  test("Non-sequential pipeline mock test") {
    val pipeline = simpleDAGPipeline().build()

    assertThrows[IllegalStateException] {
      pipeline.start(Array("-runtime", "mock"))
    }
  }

  test("Non-sequential pipeline local test") {
    val pipeline = simpleDAGPipeline(1)
        .setBufferProperty(Buffer.SERIALIZER, Serializer.BSON)
        .setBufferType(BufferType.Kafka)
        .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }
  }

  test("Simple pipeline schema exposure test (redis)") {
    val pipeline = simpleDAGPipeline(2)
      .setBufferType(BufferType.Kafka)
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE, "true")
      .build()

    assertThrows[JobExecutionException] {
      pipeline.start(Array("-runtime", "local"))
    }

    val exposer = new RedisSchemaExposer("redis://localhost:6379")

    val schema1 = exposer.get("org.codefeedr.testUtils.SimpleSourcePipelineObject")
    val schema2 = exposer.get("org.codefeedr.testUtils.SimpleTransformPipelineObject")

    assert(schema1.nonEmpty)
    assert(schema2.nonEmpty)
  }

  test("Simple pipeline schema exposure and deserialization test (redis)") {
    val pipeline = simpleDAGPipeline(2)
      .setBufferType(BufferType.Kafka)
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE, "true")
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE_DESERIALIZATION, "true")
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }
  }

  test("Simple pipeline schema exposure test (zookeeper)") {
    val pipeline = simpleDAGPipeline(2)
      .setBufferType(BufferType.Kafka)
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE, "true")
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE_SERVICE, "zookeeper")
      .setBufferProperty(KafkaBuffer.SCHEMA_EXPOSURE_HOST, "localhost:2181")
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }

    val exposer = new ZookeeperSchemaExposer("localhost:2181")

    val schema1 = exposer.get("org.codefeedr.testUtils.SimpleSourcePipelineObject")
    val schema2 = exposer.get("org.codefeedr.testUtils.SimpleTransformPipelineObject")

    assert(schema1.nonEmpty)
    assert(schema2.nonEmpty)
  }

  test("A pipeline name can be set through args") {
    val name = "nice pipeline"
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .append(new SimpleSinkPipelineObject())
      .build()

    pipeline.start(Array[String]("-runtime", "mock", "-name", name))

    assert(pipeline.name == name)
  }

  test("A pipeline has a default name") {
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .append(new SimpleSinkPipelineObject())
      .build()

    pipeline.startMock()

    assert(pipeline.name == "CodeFeedr pipeline")
  }

  /**
    * Builds a really simple (DAG) graph
    * @param expectedMessages after the job will finish.
    * @return a pipelinebuilder (add elements or finish it with .build())
    */
  def simpleDAGPipeline(expectedMessages : Int = -1) = {
    val source = new SimpleSourcePipelineObject()
    val a = new SimpleTransformPipelineObject()
    val b = new SimpleTransformPipelineObject()
    val sink = new SimpleSinkPipelineObject(expectedMessages)

    builder
      .append(source)
      .setPipelineType(PipelineType.DAG)
      .edge(source, a)
      .edge(source, b)
      .edge(a, sink)
      .edge(b, sink)
  }


  /***************** CLUSTER *********************/

  test("Cluster runtime starts the correct object") {
    val source = new HitObjectTest()
    val sink = new SimpleSinkPipelineObject(1)

    val pipeline = builder
      .setBufferType(BufferType.Kafka)
      .edge(source, sink)
      .build()

    assertThrows[CodeHitException] {
      pipeline.start(Array("-runtime", "cluster", "-stage", "org.codefeedr.testUtils.HitObjectTest"))
    }
  }

  test("Should throw when trying to start an unknown cluster object") {
    val source = new HitObjectTest()
    val sink = new SimpleSinkPipelineObject(1)

    val pipeline = builder
      .setBufferType(BufferType.Kafka)
      .edge(source, sink)
      .build()

    assertThrows[StageNotFoundException] {
      pipeline.startClustered("org.codefeedr.testUtils.DOesNotExtst")
    }
  }

  test("Should execute flink code when starting cluster object") {
    val source = new FlinkCrashObjectTest()
    val sink = new SimpleSinkPipelineObject(1)

    val pipeline = builder
      .setBufferType(BufferType.Kafka)
      .edge(source, sink)
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startClustered("org.codefeedr.testUtils.FlinkCrashObjectTest")
    }
  }

  test("Should throw when propertiesOf stage is null") {
    val sink = new SimpleSinkPipelineObject(1)
    val source = new SimpleSourcePipelineObject()

    val pipeline = builder
      .edge(source, sink)
      .build()

    assertThrows[IllegalArgumentException] {
      pipeline.propertiesOf(null)
    }
  }

  test("Show list of pipeline item ids") {
    val pipeline = builder
      .append(new StringInput())
      .append(new JsonPrinterOutput())
      .build()

    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream) {
      pipeline.start(Array("--list"))
    }

    val output = stream.toString

    assert(output.split("\n").length == 2)
    assert(output.startsWith("org.codefeedr.stages.utilities.StringInput"))
  }

  test("Show list of pipeline item ids in an exception") {
    val pipeline = builder
      .append(new StringInput())
      .append(new JsonPrinterOutput())
      .build()

    assertThrows[PipelineListException] {
      pipeline.start(Array("--list", "--asException"))
    }
  }
}
