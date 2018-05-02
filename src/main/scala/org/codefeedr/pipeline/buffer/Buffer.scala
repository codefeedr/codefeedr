package org.codefeedr.pipeline.buffer

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline

abstract class Buffer[T](pipeline: Pipeline) {
  def getSource: DataStream[T]
  def getSink: SinkFunction[T]
}
