package org.codefeedr.pipeline.buffer

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.codefeedr.pipeline.Pipeline

import scala.reflect.Manifest

class BufferFactory(pipeline: Pipeline) {

  def create[T <: AnyRef : Manifest : FromRecord](): Buffer[T] = {
    pipeline.bufferType match {
      case BufferType.None =>
        throw new RuntimeException("Cannot instantiate buffer of type 'None'")
      case BufferType.Kafka =>
        new KafkaBuffer[T](pipeline, "hallo")
    }
  }
}