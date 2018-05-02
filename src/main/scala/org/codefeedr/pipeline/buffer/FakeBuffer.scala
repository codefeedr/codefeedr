package org.codefeedr.pipeline.buffer
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline


class FakeBuffer[T](pipeline: Pipeline) extends Buffer[T](pipeline) {
  override def getSource: DataStream[T] = ???

  override def getSink: SinkFunction[T] = ???
}