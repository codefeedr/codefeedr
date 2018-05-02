package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream

abstract class Job[T <: PipelinedItem] extends PipelineObject[T, NoType] {

  override def transform(source: DataStream[T]): DataStream[NoType] = {
    main(source)

    null
  }

  def main(source: DataStream[T]): Unit
}
