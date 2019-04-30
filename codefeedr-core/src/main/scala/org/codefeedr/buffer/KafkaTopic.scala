package org.codefeedr.buffer

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** KafkaTopic stage which only embodies the topic name, to let a linked stage read from this topic. */
class KafkaTopic[T <: Serializable with AnyRef: ClassTag: TypeTag](
    topic: String)
    extends FakeStage[T](topic) {

  /** This will never be run. */
  override def main(context: Context): DataStream[T] = ???
}
