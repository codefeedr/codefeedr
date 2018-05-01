package org.codefeedr.pipeline

import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.pipeline.buffer.BufferType.BufferType

import scala.collection.mutable.ArrayBuffer

class PipelineBuilder() {
  var bufferType: BufferType = BufferType.Fake
  var objects = new ArrayBuffer[Any](0)

  def setBufferType(bufferType: BufferType): Unit = {
    this.bufferType = bufferType
  }

  def add[U <: PipelinedItem, V <: PipelinedItem](item: PipelineObject[U, V]): Unit = {
    objects += item
  }

  def pipe[U <: PipelinedItem, V <: PipelinedItem, X <: PipelinedItem, Y <: PipelinedItem](from: PipelineObject[U, V], to: PipelineObject[X, Y]) = ???

  def build(): Pipeline = {
    if (this.objects.isEmpty) {
      // TODO: EmptyPipelineException
      throw new RuntimeException("Can't build empty pipeline.")
    }

    val objects = this.objects.asInstanceOf[ArrayBuffer[PipelineObject[PipelinedItem, PipelinedItem]]]

    Pipeline(bufferType, objects)
  }
}
