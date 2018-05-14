package org.codefeedr.pipeline

import java.util.Properties

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.{StringSource, StringType}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.flink.api.scala._
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.runtime.messages.JobManagerMessages.JobResultSuccess
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, KafkaAdminClient}
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
      .append(new StringSource("hallo hallo doei doei doei"))
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
      .append(new StringSource())
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
        .setBufferType(BufferType.Kafka)
        .build()

    assertThrows[JobExecutionException] {
      pipeline.start(Array("-runtime", "local"))
    }
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
      .extraEdge(b, sink)
  }


}
