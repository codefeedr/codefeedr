package org.codefeedr.pipeline

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._

abstract class PipelineObject[In <: PipelineItem : ClassTag : Manifest, Out <: PipelineItem : ClassTag : Manifest] {
  var pipeline: Pipeline = _

  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  def transform(source: DataStream[In]): DataStream[Out]

  def tearDown(): Unit = {
    this.pipeline = null
  }

  def hasMainSource: Boolean = typeOf[In] != typeOf[NoType]

  def hasSink: Boolean = typeOf[Out] != typeOf[NoType]

  def getMainSource: DataStream[In] = {
    assert(pipeline != null)

    if (!hasMainSource) {
      throw NoSourceException("PipelineObject defined NoType as In type. Buffer can't be created.")
    }

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[In]()

    buffer.getSource
  }

  def getSink: SinkFunction[Out] = {
    assert(pipeline != null)

    if (!hasSink) {
      throw NoSinkException("PipelineObject defined NoType as Out type. Buffer can't be created.")
    }

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[Out]()

    buffer.getSink
  }

  def getStorageSource[T](typ: String, collection: String): DataStream[T] = ???

  def getStorageSink[T](typ: String, collection: String): DataSink[T] = ???

  // Returns data source based on the sink of the PO given. Find the PO in the nodes, and then get topic etc
//  def getSource[T](objectClass: poClass): DataStream[T] = ???
}
