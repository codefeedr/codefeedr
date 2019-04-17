package org.codefeedr.plugins.pypi

import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiReleaseExt
import org.codefeedr.plugins.pypi.stages.{
  PyPiReleaseExtStage,
  PyPiReleasesStage
}
import org.codefeedr.stages.utilities.PrinterOutput

object Main {
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .append(new PyPiReleasesStage)
      .append(new PyPiReleaseExtStage)
      .append(new PrinterOutput[PyPiReleaseExt]())
      .build()
      .startMock()
  }
}
