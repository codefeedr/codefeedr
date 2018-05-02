package org.codefeedr.pipeline

import com.sun.tools.javac.code.TypeTag
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

import scala.reflect.{ClassTag, classTag}

abstract class PipelineObject[In <: PipelinedItem : ClassTag, Out <: PipelinedItem : ClassTag] {
  var pipeline: Pipeline = null

  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  def transform(source: DataStream[In]): DataStream[Out]

  def tearDown(): Unit = {
    this.pipeline = null
  }


//  implicit object NoTypeSource extends PipelineObject[NoType, Out] {
//    def smaller = (a:Int, b:Int) => (a < b)
//  }

  // TODO: disallow In = NoType    (implict ev: In =:= NoType = null)
//  def getSource[In: TypeTag]: DataStream[In] = {
  def getSource: DataStream[In] = {
    assert(pipeline != null)


  if (classTag[In] == classTag[NoType]) {
      print("NO TYPE SOURCE!")
    }

    // Look up what source there is
    // if In == NoType then THROW
    // else make buffer

    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[In]()

    buffer.getSource
  }

  def getSink: SinkFunction[Out] = {
    val factory = new BufferFactory(pipeline)
    val buffer = factory.create[Out]()

    buffer.getSink
  }

  def getStorageSource[T](typ: String, collection: String): DataStream[T] = ???

  def getStorageSink[T](typ: String, collection: String): DataSink[T] = ???
}
