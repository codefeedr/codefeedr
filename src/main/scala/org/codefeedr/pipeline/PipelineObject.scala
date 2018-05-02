package org.codefeedr.pipeline

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._

abstract class PipelineObject[In <: PipelinedItem : ClassTag : Manifest, Out <: PipelinedItem : ClassTag : Manifest] {
  var pipeline: Pipeline = _

  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  def transform(source: DataStream[In]): DataStream[Out]

  def tearDown(): Unit = {
    this.pipeline = null
  }

  def hasSource: Boolean = typeOf[In] != typeOf[NoType]

  def hasSink: Boolean = typeOf[Out] != typeOf[NoType]

  def getSource: DataStream[In] = {
    assert(pipeline != null)

    if (!hasSource) {
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
}
