package org.codefeedr.plugins.pypi.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.datastream.{
  AsyncDataStream => JavaAsyncDataStream
}
import org.apache.flink.streaming.api.functions.async.{
  AsyncFunction => JavaAsyncFunction
}
import org.codefeedr.plugins.pypi.protocol.Protocol.{
  PyPiRelease,
  PyPiReleaseExt
}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.codefeedr.plugins.pypi.operators.RetrieveProjectAsync

class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiReleaseExt](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiReleaseExt] = {

    val async = JavaAsyncDataStream.orderedWait(source.javaStream,
                                                new RetrieveProjectAsync,
                                                5,
                                                TimeUnit.SECONDS,
                                                100)

    new org.apache.flink.streaming.api.scala.DataStream(async)
      .map(x => (x.project.info.name, x.project.releases))
      .print()

    new org.apache.flink.streaming.api.scala.DataStream(async)
  }
}
