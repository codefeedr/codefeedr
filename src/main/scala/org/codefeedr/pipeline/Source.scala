package org.codefeedr.pipeline

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.{ClassTag, Manifest}


/**
  * The Source class represents the start of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam Out the output type of the job.
  */
abstract class Source[Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[NoType, Out] {

  override def transform(source: DataStream[NoType]): DataStream[Out] = {
    main()
  }

  /**
    * Create a new datastream
    *
    * @return Stream
    */
  def main(): DataStream[Out]
}
