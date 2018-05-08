package org.codefeedr

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.keymanager.redis.RedisKeyManager
import org.codefeedr.pipeline._
import org.codefeedr.pipeline.buffer.serialization.Serializer
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.{StringSource, StringType}
import org.scalatest.FunSuite

class MyJob extends Job[StringType] {

  override def main(source: DataStream[StringType]): Unit = {
    source
      .map { item => (item.value.length, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }

}

class MainTest extends FunSuite {

  test("Run a thing") {
    new PipelineBuilder()

      .setBufferType(BufferType.Kafka)
      .setBufferProperty(KafkaBuffer.HOST, "localhost:9092")
      .setBufferProperty(KafkaBuffer.SERIALIZER, Serializer.AVRO)
      .setKeyManager(new RedisKeyManager("redis://localhost:6379"))

      .append(new StringSource())
      .append(new MyJob())

      .build()

      .startMock()
  }
}