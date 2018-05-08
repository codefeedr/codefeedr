package org.codefeedr.testUtils

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.{NoType, PipelineItem, PipelineObject}

case class StringType(value: String) extends PipelineItem

class EmptySourcePipelineObject extends PipelineObject[NoType, StringType] {
  override def transform(source: DataStream[NoType]): DataStream[StringType] = ???
}

class EmptyTransformPipelineObject extends PipelineObject[StringType, StringType] {
  override def transform(source: DataStream[StringType]): DataStream[StringType] = ???
}

class EmptySinkPipelineObject extends PipelineObject[StringType, NoType] {
  override def transform(source: DataStream[StringType]): DataStream[NoType] = ???
}