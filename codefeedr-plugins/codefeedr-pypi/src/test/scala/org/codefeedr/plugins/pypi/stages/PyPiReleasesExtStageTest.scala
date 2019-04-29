package org.codefeedr.plugins.pypi.stages

import java.util
import java.util.Date

import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.pypi.protocol.Protocol.{
  PyPiRelease,
  PyPiReleaseExt
}
import org.scalatest.FunSuite
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream

class PyPiReleasesExtStageTest extends FunSuite {

  test("PyPiReleasesExtStage integration test") {
    val release = new PyPiRelease("invenio-github 1.0.0a16", "", "", new Date())

    new PipelineBuilder()
      .appendSource(x => x.fromCollection(List(release)))
      .append(new PyPiReleaseExtStage())
      .append { x: DataStream[PyPiReleaseExt] =>
        x.addSink(new CollectReleases)
      }
      .build()
      .startMock()

    assert(CollectReleases.result.size() == 1)
  }
}

object CollectReleases {
  val result = new util.ArrayList[PyPiReleaseExt]()
}

class CollectReleases extends SinkFunction[PyPiReleaseExt] {
  override def invoke(value: PyPiReleaseExt,
                      context: SinkFunction.Context[_]): Unit = {
    CollectReleases.result.add(value)
  }
}
