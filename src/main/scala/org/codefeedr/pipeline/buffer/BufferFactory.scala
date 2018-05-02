package org.codefeedr.pipeline.buffer

import org.codefeedr.pipeline.Pipeline

class BufferFactory(pipeline: Pipeline) {

  def create[T](): Buffer[T] = {
    pipeline.bufferType match {
      case BufferType.Fake =>
        new FakeBuffer[T](pipeline)
      case BufferType.Kafka =>
        new KafkaBuffer[T](pipeline, "hallo")
    }
  }
}