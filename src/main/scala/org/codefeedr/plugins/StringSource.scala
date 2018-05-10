package org.codefeedr.plugins

import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.codefeedr.pipeline.{NoType, PipelineItem, PipelineObject}

case class StringType(value: String) extends PipelineItem

class StringSource(str : String = "") extends PipelineObject[NoType, StringType] {

  override def transform(source: DataStream[NoType]): DataStream[StringType] = {
    val list = str.split("[ \n]")

    pipeline.environment
      .fromCollection(list).setParallelism(1)
      .map { str => StringType(str) }.setParallelism(1)
  }

}