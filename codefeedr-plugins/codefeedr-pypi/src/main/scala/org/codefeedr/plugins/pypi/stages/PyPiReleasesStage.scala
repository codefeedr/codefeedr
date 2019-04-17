package org.codefeedr.plugins.pypi.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiRelease
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._
import org.codefeedr.plugins.pypi.operators.PyPiReleasesSource

class PyPiReleasesStage(stageId: String = "pypi_releases_min")
    extends InputStage[PyPiRelease](Some(stageId)) {
  override def main(context: Context): DataStream[PyPiRelease] = {
    val str = context.env
      .addSource(new PyPiReleasesSource())

    str
  }
}
