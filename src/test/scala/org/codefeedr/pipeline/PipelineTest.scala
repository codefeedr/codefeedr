package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.{StringSource, StringType}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.sink.SinkFunction

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
      .append { x : DataStream[StringType] =>
          x.map(x => (x.value, 1))
          .keyBy(0)
          .sum(1)
          .map(x => WordCount(x._1, x._2))
      }
      .append { x : DataStream[WordCount] =>
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
        x.map(x => WordCount(x.value, 1))
          .addSink(new CollectSink)
      }
      .build()
      .startMock()

    val res = CollectSink.result.asScala

    assert(res.contains(WordCount("",1)))
  }

}
