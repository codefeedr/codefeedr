package org.codefeedr.pipeline.buffer
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream


class FakeBuffer[T] extends Buffer[T] {
  override def getSource: DataStream[T] = ???

  override def getSink: DataSink[T] = ???
}