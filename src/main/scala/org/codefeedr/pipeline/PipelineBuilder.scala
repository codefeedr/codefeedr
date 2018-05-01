package org.codefeedr.pipeline

class PipelineBuilder() {

  def setBufferType[T](cls: Class[T]) = {

  }

  def pipe[U <: PipelinedItem, X <: PipelinedItem, V <: PipelinedItem](from: PipelineObject[U, X], to: PipelineObject[X, V]) = {

  }

  def build(): Pipeline = {
    new Pipeline()
  }
}
