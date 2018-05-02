package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.codefeedr.pipeline.buffer.BufferType.BufferType

case class Pipeline(bufferType: BufferType, objects: Seq[PipelineObject[PipelinedItem, PipelinedItem]]) {

  def start(args: Array[String]) = {
    val env = getEnvironment

    // TODO: clustered
    for (obj <- objects) {
      obj.setUp(this)
    }

    for (obj <- objects) {
      val result = obj.main(obj.getSource)

      // TODO: check for NoType

      result.addSink(obj.getSink)
    }

    env.execute("CodeFeedr Job")
  }

  def getEnvironment : StreamExecutionEnvironment = {
    StreamExecutionEnvironment.getExecutionEnvironment
  }

}