package org.codefeedr.testUtils

import org.apache.flink.api.common.JobID
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.api.scala._
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.functions.sink.{PrintSinkFunction, RichSinkFunction, SinkFunction}
import org.codefeedr.pipeline.{Job, NoType, PipelineItem, PipelineObject}

case class StringType(value: String) extends PipelineItem
final case class JobFinishedException() extends JobExecutionException(new JobID(), "Job is finished.")

class EmptySourcePipelineObject extends PipelineObject[NoType, StringType] {
  override def transform(source: DataStream[NoType]): DataStream[StringType] = {
    pipeline.environment.addSource {
      new RichSourceFunction[StringType] {
        override def run(ctx: SourceFunction.SourceContext[StringType]): Unit =  {
          ctx.collect(StringType("hallo"))
          ctx.collect(StringType("hallo2"))
          ctx.close()
        }

        override def cancel(): Unit = {}
      }
    }
  }

}

class EmptyTransformPipelineObject extends PipelineObject[StringType, StringType] {
  override def transform(source: DataStream[StringType]): DataStream[StringType] = source
}

class EmptySinkPipelineObject(elements : Int = -1) extends Job[StringType] {
  override def main(source: DataStream[StringType]): Unit = {
    source.addSink(new PrintSinkElements(elements)).setParallelism(1)
  }
}

class PrintSinkElements(elements : Int) extends PrintSinkFunction[StringType] {
  var count = 0

  def invoke(value: StringType, context: SinkFunction.Context[_]): Unit = {
    super.invoke(value)
    count += 1

    println(s"Current count: $count")

    if (elements != -1 && count >= elements) {
      throw new JobFinishedException()
    }
  }
}