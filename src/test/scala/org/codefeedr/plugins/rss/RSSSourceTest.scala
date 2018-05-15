package org.codefeedr.plugins.rss

import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.functions.sink.{PrintSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.{Job, PipelineBuilder}
import org.codefeedr.testUtils.{JobFinishedException, SimpleSinkPipelineObject}
import org.scalatest.FunSuite

class RSSSourceTest extends FunSuite {

  test("RSS source end-to-end test") {
    val rssURL = "http://lorem-rss.herokuapp.com/feed?unit=second"
    val source = new RSSSource(rssURL, 1000)
    val sink = new LimitingSinkPipelineObject(12)

    val pipeline = new PipelineBuilder()
      .append(source)
      .append(sink)
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startMock()
    }

    //Assert that all items in the pipeline are unique
    assert(sink.getItems().distinct == sink.getItems())
  }
}

// Simple Sink Pipeline Object that limits the output to a certain number
// and is able to get a list of all the items that were received in the sink
class LimitingSinkPipelineObject(elements: Int = -1) extends Job[RSSItem] with Serializable {
  var sink: LimitingSink = _

  override def main(source: DataStream[RSSItem]): Unit = {
    sink = new LimitingSink(elements)
    source.addSink(sink).setParallelism(1)
  }

  def getItems(): List[RSSItem] = {
    sink.items
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

