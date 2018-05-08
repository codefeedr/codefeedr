package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.{StringSource, StringType}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.flink.api.scala._

import scala.collection.JavaConverters._

class PipelineTest extends FunSuite with BeforeAndAfter {

  var builder: PipelineBuilder = _

  before {
    builder = new PipelineBuilder()
  }

  test("Simple pipeline test") {
    builder
      .append(new StringSource("Hallo hallo doei doei doei"))
      .append { x : DataStream[StringType] =>
          x.flatMap(_.value.toLowerCase.split("\\\\W+"))
          .filter(_.nonEmpty)
          .map((_, 1))
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

}
