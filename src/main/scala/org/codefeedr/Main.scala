package org.codefeedr

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline._
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.{StringSource, StringType}
import org.codefeedr.plugins.rss._

class MyJob extends Job[StringType] {

  override def main(source: DataStream[StringType]): Unit = {
    source
      .map { item => (item.value.length, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }

}

object Main {

  def main(args: Array[String]): Unit = {
    // Create pipeline
    val builder = new PipelineBuilder()
    builder.setBufferType(BufferType.Kafka)
    builder.bufferProperties.set(KafkaBuffer.HOST, "localhost:9092")

    builder.add(new StringSource())
    builder.add(new MyJob())

    val pipeline = builder.build()

    // Run
    pipeline.start(args)
  }
}