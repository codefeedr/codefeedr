package org.codefeedr.pipeline

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.{ClassTag, Manifest}

/**
  * The Job class represents the end of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In the input type of the job.
  */
abstract class Job[In <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, NoType] {

  override def transform(source: DataStream[In]): DataStream[NoType] = {
    main(source)

    null
  }

  /**
    * Use the given datastream
    * 
    * @param source
    */
  def main(source: DataStream[In]): Unit
}
