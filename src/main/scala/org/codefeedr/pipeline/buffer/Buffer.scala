package org.codefeedr.pipeline.buffer

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream


trait Buffer[T] {
  def createSource(): DataStream[T]
  def createSink(): DataSink[T]
}
