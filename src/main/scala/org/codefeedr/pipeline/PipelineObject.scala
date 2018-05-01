package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

abstract class PipelineObject[In <: PipelinedItem, Out <: PipelinedItem] {
  var pipeline: Pipeline = null

  def setup() {}

  def main(pipeline: Pipeline)

  //  def getStorageSource[T](typ: String, collection: String): DataStream[T] = {
  //
  //  }
  //
  //  def getStorageSink[T](typ: String, collection: String): DataSink[T] = {
  //
  //  }
  //
    def getSource(): DataStream[In] = {
      assert(pipeline != null)

      // Look up what source there is
      // if In == NoType then THROW
      // else make buffer

      val factory = new BufferFactory(pipeline)
      val buffer = factory.create[In]()
      val source = buffer.getSource

      source
    }

//    def getSink(): DataSink[Out] = {
//
//    }

  // TODO: Add pipeline building with ->
}
