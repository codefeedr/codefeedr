package org.codefeedr.pipeline.buffer
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.scala.DataStream


class FakeBuffer[T] extends Buffer[T] {
  override def createSource(): DataStream[T] = ???

  override def createSink(): DataSink[T] = ???
}