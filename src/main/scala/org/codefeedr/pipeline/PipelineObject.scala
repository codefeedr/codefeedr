package org.codefeedr.pipeline

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory
import scala.reflect.Manifest

import scala.reflect.{ClassTag, classTag}

abstract class PipelineObject[In <: PipelinedItem : ClassTag : Manifest, Out <: PipelinedItem : ClassTag : Manifest] {
  var pipeline: Pipeline = _

  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  def transform(source: DataStream[In]): DataStream[Out]

  def tearDown(): Unit = {
    this.pipeline = null
  }

  // TODO: disallow In = NoType    (implict ev: In =:= NoType = null)
  def getSource: DataStream[In] = {
    assert(pipeline != null)

    if (classTag[In] == classTag[NoType]) {
      throw NoSourceException("PipelineObject defined NoType as In type. Buffer can't be created.")
    }

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[In]()

    buffer.getSource
  }

  def getSink: SinkFunction[Out] = {
    assert(pipeline != null)

    if (classTag[Out] == classTag[NoType]) {
      throw NoSinkException("PipelineObject defined NoType as Out type. Buffer can't be created.")
    }

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[Out]()

    buffer.getSink
  }

  def getStorageSource[T](typ: String, collection: String): DataStream[T] = ???

  def getStorageSink[T](typ: String, collection: String): DataSink[T] = ???
}
