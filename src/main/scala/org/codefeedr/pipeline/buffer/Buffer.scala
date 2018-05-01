package org.codefeedr.pipeline.buffer

import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline

abstract class Buffer[T](pipeline: Pipeline) {
  def getSource: DataStream[T]
  def getSink: DataSink[T]
}
