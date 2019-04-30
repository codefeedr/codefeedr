package org.codefeedr.buffer

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class KafkaTopic[T <: Serializable with AnyRef: ClassTag: TypeTag](
    topic: String)
    extends FakeStage[T](topic) {

  override def main(context: Context): DataStream[T] = ???
}
