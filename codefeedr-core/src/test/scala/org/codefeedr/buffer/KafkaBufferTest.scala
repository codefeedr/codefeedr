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
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.{InputStage, OutputStage, StageAttributes}
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
    val id = UUID.randomUUID().toString
    val numberOutput = new NumberOutput(StageAttributes(Some(id)))
    val numberInput = new NumberInput()

    val pipeline = new PipelineBuilder()
      .append(numberInput) //pushes 1 till 50
      .append(numberOutput) //reads and crashes
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }

    assertThrows[JobExecutionException] {
      numberInput.numberSource.switch = false
      val pipeline = new PipelineBuilder()
        .append(numberInput) //pushes 51 till 100
        .append(numberOutput) //reads and crashes
        .build()
        .startLocal()
    }

    assert(StringCollectSink.asList.distinct.size == 100)
  }

  test("Giving properties with a kafka buffer should override default properties") {
    val emptyProperties = new org.codefeedr.Properties()

    val kafkaBuffer = new KafkaBuffer[StringType](null, emptyProperties, null, null, "test")
    val correctDefaultProperties = new java.util.Properties()
    correctDefaultProperties.put("bootstrap.servers", "localhost:9092")
    correctDefaultProperties.put("zookeeper.connect", "localhost:2181")
    correctDefaultProperties.put("auto.offset.reset", "earliest")
    correctDefaultProperties.put("auto.commit.interval.ms", "100")
    correctDefaultProperties.put("enable.auto.commit", "true")
    correctDefaultProperties.put("group.id", "test")
    assert(kafkaBuffer.getKafkaProperties == correctDefaultProperties)


    val properties = new org.codefeedr.Properties()
      .set(KafkaBuffer.BROKER, "nonlocalhost:9092")
      .set(KafkaBuffer.ZOOKEEPER, "nonlocalhost:2181")
      .set("auto.commit.interval.ms", "200")
      .set("some.other.property", "some-value")


    val kafkaBuffer2 = new KafkaBuffer[StringType](null, properties, null, null, "test")
    val correctProperties = new java.util.Properties()
    correctProperties.put("bootstrap.servers", "nonlocalhost:9092")
    correctProperties.put("zookeeper.connect", "nonlocalhost:2181")
    correctProperties.put("auto.offset.reset", "earliest")
    correctProperties.put("auto.commit.interval.ms", "200")
    correctProperties.put("enable.auto.commit", "true")
    correctProperties.put("group.id", "test")
    correctProperties.put("some.other.property", "some-value")
    assert(kafkaBuffer2.getKafkaProperties == correctProperties)

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

class NumberInput() extends InputStage[StringType] {
  val idd = UUID.randomUUID().toString
  val numberSource = new NumberSource()

  override def main(): DataStream[StringType] = {
    pipeline.environment.addSource(numberSource)
  }

  override def getSinkSubject: String = {
    idd
  }
}

class NumberSource() extends SourceFunction[StringType] {
  var switch = true
  override def run(ctx: SourceFunction.SourceContext[StringType]): Unit = {
    println(s"Now here, switch is $switch")
    if (switch) {
      for (i <- 1 to 50) {
        ctx.collect(StringType(i.toString))
      }
    } else {
      for (i <- 51 to 100) {
        ctx.collect(StringType(i.toString))
      }
    }
  }
  override def cancel(): Unit = {}
}

class NumberOutput(stageAttributes: StageAttributes) extends OutputStage[StringType](stageAttributes) {
  override def main(source: DataStream[StringType]): Unit = source.addSink(new StringCollectSink)
}

case class TestEvent(name: String, time: Date)