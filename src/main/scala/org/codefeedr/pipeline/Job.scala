package org.codefeedr.pipeline

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.{ClassTag, Manifest}

/**
  * The Job class represents the end of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam T the input type of the job.
  */
abstract class Job[T <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[T, NoType] {

  /**
    * Transform a (buffer) input source to its final result.
    * In this case the Job is the end of the pipeline.
    *
    * @param source the input source.
    * @return the transformed stream.
    */
  override def transform(source: DataStream[T]): DataStream[NoType] = {
    main(source)

    null
  }

  def main(source: DataStream[T]): Unit
}
