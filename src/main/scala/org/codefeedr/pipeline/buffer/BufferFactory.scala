package org.codefeedr.pipeline.buffer

import com.sksamuel.avro4s.FromRecord
import org.codefeedr.pipeline.{Pipeline, PipelineItem, PipelineObject}

import scala.reflect.Manifest

/**
  * Buffer creator.
  *
  * Creates a buffer that reflects the sink of given object.
  *
  * If an in-buffer is needed: sinkObject should be the parent
  * If an out-buffer is needed: sinkObject should be the actual node that needs a sink
  *
  * @param pipeline Pipeline
  * @param sinkObject Object that writes to the buffer
  */
class BufferFactory[U <: PipelineItem, V <: PipelineItem](pipeline: Pipeline, sinkObject: PipelineObject[U, V]) {

  /**
    * Create a new buffer
    *
    * @tparam T Object type within the buffer
    * @return Buffer
    * @throws IllegalArgumentException When sinkObject is null
    * @throws IllegalStateException When buffer could not be instantiated due to bad configuration
    */
  def create[T <: AnyRef : Manifest : FromRecord](): Buffer[T] = {
    if (sinkObject == null) {
      throw new IllegalArgumentException("Buffer factory requires a sink object to determine buffer location")
    }

    val subject = sinkObject.getSinkSubject

    println(s"Creating new buffer with subject: $subject")
    pipeline.bufferType match {
      case BufferType.None =>
        throw new IllegalStateException("Cannot instantiate buffer of type 'None'")
      case BufferType.Kafka =>
        new KafkaBuffer[T](pipeline, subject)
    }
  }
}