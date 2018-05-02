package org.codefeedr.pipeline

import org.apache.flink.api.common.typeinfo.TypeInformation

import org.apache.flink.streaming.api.scala.DataStream
import scala.reflect.Manifest

import scala.reflect.ClassTag

abstract class Job[T <: PipelinedItem : ClassTag : Manifest] extends PipelineObject[T, NoType] {

  override def transform(source: DataStream[T]): DataStream[NoType] = {
    main(source)

    null
  }

  def main(source: DataStream[T]): Unit
}
