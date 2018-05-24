package org.codefeedr.plugins.rss

import org.apache.flink.api.common.JobID
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.OutputStage
import org.scalatest.FunSuite

//This will be thrown after the print sink received x elements.
final case class JobFinishedException() extends JobExecutionException(new JobID(), "Job is finished.")
class RSSInputStageTest extends FunSuite {

  test("RSS source end-to-end test") {
    val rssURL = "http://lorem-rss.herokuapp.com/feed?unit=second"
    val source = new RSSInputStage(rssURL, "EEE, dd MMMM yyyy HH:mm:ss z", 1000)
    val sink = new LimitingSinkPipelineObject(12)

    val pipeline = new PipelineBuilder()
      .append(source)
      .append(sink)
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startMock()
    }

    //Assert that all items in the pipeline are unique
//    assert(sink.getItems().distinct == sink.getItems())
//    assert(sink.getItems().size == 12)
  }
}

// Simple Sink Pipeline Object that limits the output to a certain number
// and is able to get a list of all the items that were received in the sink
class LimitingSinkPipelineObject(elements: Int = -1) extends OutputStage[RSSItem] with Serializable {
  var sink: LimitingSink = _

  override def main(source: DataStream[RSSItem]): Unit = {
    sink = new LimitingSink(elements)
    source.addSink(sink).setParallelism(1)
  }
}

class LimitingSink(elements: Int) extends SinkFunction[RSSItem] {
  var count = 0
  var items: List[RSSItem] = List()

  override def invoke(value: RSSItem, context: SinkFunction.Context[_]): Unit = {
    count += 1
    items = value :: items

    if (elements != -1 && count >= elements) {
      throw JobFinishedException()
    }
  }
}

