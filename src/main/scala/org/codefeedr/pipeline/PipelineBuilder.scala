package org.codefeedr.pipeline

import org.codefeedr.Properties
import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.pipeline.buffer.BufferType.BufferType

import scala.collection.mutable.ArrayBuffer

class PipelineBuilder() {
  /** Type of buffer used in the pipeline */
  protected var bufferType: BufferType = BufferType.Fake

  /** Properties of the buffer */
  val bufferProperties = new Properties()

  /** Pipeline objects */
  protected var objects = new ArrayBuffer[Any](0)

  /** Pipeline properties */
  val properties = new Properties()

  def getBufferType: BufferType = {
    bufferType
  }

  def setBufferType(bufferType: BufferType): Unit = {
    this.bufferType = bufferType
  }

  def add[U <: PipelinedItem, V <: PipelinedItem](item: PipelineObject[U, V]): Unit = {
    objects += item
  }

  def build(): Pipeline = {
    if (this.objects.isEmpty) {
      throw EmptyPipelineException()
    }

    val objects = this.objects.asInstanceOf[ArrayBuffer[PipelineObject[PipelinedItem, PipelinedItem]]]

    Pipeline(bufferType, bufferProperties.toImmutable, objects, properties.toImmutable)
  }
}
