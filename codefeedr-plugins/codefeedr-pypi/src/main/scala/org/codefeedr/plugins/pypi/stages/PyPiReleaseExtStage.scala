package org.codefeedr.plugins.pypi.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream}
import org.codefeedr.plugins.pypi.protocol.Protocol.{PyPiProject, PyPiRelease}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiProject](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiProject] = {

    AsyncDataStream.orderedWait(source,
                                new MapReleaseToProject,
                                5,
                                TimeUnit.SECONDS,
                                100)
  }
}

/** Maps a [[PyPiRelease]] to a [[PyPiProject]]. */
class MapReleaseToProject extends AsyncFunction[PyPiRelease, PyPiProject] {

  override def asyncInvoke(input: PyPiRelease,
                           resultFuture: ResultFuture[PyPiProject]): Unit = {}

  override def timeout(input: PyPiRelease,
                       resultFuture: ResultFuture[PyPiProject]): Unit = {}
}
