package org.codefeedr.plugins.pypi

import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.pypi.stages.{
  PyPiReleaseExtStage,
  PyPiReleasesStage
}

object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new PyPiReleasesStage)
      .append(new PyPiReleaseExtStage)
      .build()
      .startMock()
  }
}
