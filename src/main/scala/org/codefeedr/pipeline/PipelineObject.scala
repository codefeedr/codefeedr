package org.codefeedr.pipeline

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

abstract class PipelineObject[In <: PipelinedItem, Out <: PipelinedItem] {
  var pipeline: Pipeline = null

  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  def main()

  def tearDown(): Unit = {
    this.pipeline = null
  }

  // TODO: disallow In = NoType    (implict ev: In =:= NoType = null)
  def getSource: DataStream[In] = {
    assert(pipeline != null)

    // Look up what source there is
    // if In == NoType then THROW
    // else make buffer

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[In]()
    val source = buffer.getSource

    source
  }

  def getSink: DataSink[Out] = {
    null
  }

  def getStorageSource[T](typ: String, collection: String): DataStream[T] = ???

  def getStorageSink[T](typ: String, collection: String): DataSink[T] = ???
}
