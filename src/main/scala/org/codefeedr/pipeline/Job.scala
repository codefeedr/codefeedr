package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.ClassTag

abstract class Job[T <: PipelinedItem : ClassTag] extends PipelineObject[T, NoType] {

  override def transform(source: DataStream[T]): DataStream[NoType] = {
    main(source)

    null
  }

  def main(source: DataStream[T]): Unit
}
