package org.codefeedr.plugins.pypi.stages

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.pypi.protocol.Protocol.{PyPiProject, PyPiRelease}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._

class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiProject](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiProject] = {
    source.map(new MapReleaseToProject)
  }
}

class MapReleaseToProject extends RichMapFunction[PyPiRelease, PyPiProject] {
  override def map(value: PyPiRelease): PyPiProject = {}
}
