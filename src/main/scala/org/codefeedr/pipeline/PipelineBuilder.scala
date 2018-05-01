package org.codefeedr.pipeline

import org.codefeedr.pipeline.BufferType.BufferType

import scala.collection.mutable.ArrayBuffer

object BufferType extends Enumeration {
  type BufferType = Value
  val Fake, Kafka = Value
}

class PipelineBuilder() {
  var bufferType: BufferType = BufferType.Fake
  var objects = new ArrayBuffer[Any](0)

  def setBufferType(bufferType: BufferType) = {
    this.bufferType = bufferType
  }

  def add[U <: PipelinedItem, V <: PipelinedItem](item: PipelineObject[U, V]): Unit = {
    objects += item
  }

  def pipe[U <: PipelinedItem, V <: PipelinedItem, X <: PipelinedItem, Y <: PipelinedItem](from: PipelineObject[U, V], to: PipelineObject[X, Y]) = {

  }


  def build(): Pipeline = {
    if (objects.isEmpty) {
      // TODO: EmptyPipelineException
      throw new RuntimeException("Can't build empty pipeline.")
    }

//    Pipeline(bufferType, objects.asInstanceOf[Array[PipelineObject[PipelinedItem, PipelinedItem]]])
    Pipeline(bufferType, objects.asInstanceOf[ArrayBuffer[PipelineObject[PipelinedItem, PipelinedItem]]])
  }
}
