package org.codefeedr.pipeline.buffer

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline

import scala.reflect.ClassTag
import scala.reflect.classTag


abstract class Buffer[T](pipeline: Pipeline) {
  //Get type of the class at run time
//  val inputClassType : Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  def getSource: DataStream[T]
  def getSink: DataSink[T]
}
