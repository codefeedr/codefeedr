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

import java.util
import java.util.{Date, Properties, UUID}

import scala.collection.JavaConversions._
import org.apache.flink.api.scala._
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.functions.sink.SinkFunction.Context
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig}
import org.codefeedr.pipeline.{PipelineBuilder, PipelineItem}
import org.codefeedr.stages.{InputStage, StageAttributes}
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.{JobFinishedException, SimpleSourcePipelineObject}
import org.scalatest.{BeforeAndAfter, FunSuite}

class KafkaBufferTest extends FunSuite with BeforeAndAfter {

  var client : AdminClient = _
  var kafkaBuffer : KafkaBuffer[StringType] = _

  before {
    //set all the correct properties
    val props = new Properties()
    props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

    //connect with Kafka
    client = AdminClient.create(props)

    //setup simple kafkabuffer
    val pipeline = new PipelineBuilder().append(new SimpleSourcePipelineObject()).build()
    kafkaBuffer = new KafkaBuffer[StringType](pipeline, pipeline.bufferProperties, StageAttributes(),"test-subject", null)

  }


  test ("A topic not existing should be created") {
    val uuid = UUID.randomUUID().toString //random topic

    assert(!exists(uuid))
    kafkaBuffer.checkAndCreateSubject(uuid, "localhost:9092")
    assert(exists(uuid))
  }

  test ("A non-existent schema should throw an exception") {
    assertThrows[SchemaNotFoundException] {
      kafkaBuffer.getSchema("nOnExistent")
    }
  }

  test ("A schema should correctly be exposed") {
    assert(kafkaBuffer.exposeSchema())
  }

  /**
    * Check if topic exists.
    * @param topic topic to check.
    * @return if Kafka registered the topic.
    */
  def exists(topic : String) : Boolean = {
    client
      .listTopics()
      .names()
      .get()
      .contains(topic)
  }

  test("Stage should read from kafka where it left off") {

    val pipeline = new PipelineBuilder()
      .append(new NumberInput)
      .append{ x: DataStream[StringType] =>
        x.addSink(new StringCollectSink)
      }
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }

    assertThrows[JobExecutionException] {
      pipeline.startClustered("org.codefeedr.pipeline.PipelineBuilder$$anon$1")
    }

    assert(StringCollectSink.asList.distinct.size == 100)
  }

}

object StringCollectSink {
  var result = new util.ArrayList[String]() //mutable list

  def reset(): Unit = {
    result = new util.ArrayList[String]()
  }

  def asList: List[String] = result.toList
}

class StringCollectSink extends SinkFunction[StringType] {

  override def invoke(value: StringType, context: Context[_]): Unit = {
    synchronized {
      StringCollectSink.result.add(value.value)
      if (StringCollectSink.result.size() == 50 || StringCollectSink.result.size() == 100) {
        throw JobFinishedException()
      }
    }
  }
}

class NumberInput extends InputStage[StringType] {
  override def main(): DataStream[StringType] = {
    pipeline.environment.addSource(new NumberSource)
  }
}

class NumberSource extends SourceFunction[StringType] {
  override def run(ctx: SourceFunction.SourceContext[StringType]): Unit = {
    for (i <- 1 to 100) {
      ctx.collect(StringType(i.toString))
    }
  }
  override def cancel(): Unit = {}
}

case class TestEvent(name: String, time: Date) extends PipelineItem