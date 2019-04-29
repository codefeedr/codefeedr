package org.codefeedr.plugins.pypi.stages

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.pypi.operators.{
  PyPiReleasesSource,
  PyPiSourceConfig
}
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiRelease
import org.codefeedr.stages.OutputStage
import org.codefeedr.testUtils.JobFinishedException
import org.scalatest.FunSuite

class PyPiReleasesStageTest extends FunSuite {

  test("PyPiIntegrationTest") {
    val source =
      new PyPiReleasesStage(sourceConfig = PyPiSourceConfig(1000, 12))
    val sink = new LimitingSinkStage(12)

    val pipeline = new PipelineBuilder()
      .append(source)
      .append(sink)
      .build()
      .startMock()
  }

}

// Simple Sink Pipeline Object that limits the output to a certain number
// and is able to get a list of all the items that were received in the sink
class LimitingSinkStage(elements: Int = -1)
    extends OutputStage[PyPiRelease]
    with Serializable {
  var sink: LimitingSink = _

  override def main(source: DataStream[PyPiRelease]): Unit = {
    sink = new LimitingSink(elements)
    source.addSink(sink).setParallelism(1)
  }
}

class LimitingSink(elements: Int) extends SinkFunction[PyPiRelease] {
  var count = 0
  var items: List[PyPiRelease] = List()

  override def invoke(value: PyPiRelease,
                      context: SinkFunction.Context[_]): Unit = {
    count += 1
    items = value :: items

    println(count)

    if (elements != -1 && count >= elements) {
      throw new JobFinishedException()
    }
  }
}
