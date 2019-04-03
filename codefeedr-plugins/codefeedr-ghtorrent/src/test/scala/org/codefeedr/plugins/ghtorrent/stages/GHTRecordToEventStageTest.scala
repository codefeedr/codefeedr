package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.ghtorrent.GHTTestSource
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.{Event, Record}
import org.scalatest.FunSuite
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.ghtorrent.protocol.GitHub.{CreateEvent, PushEvent}

class GHTRecordToEventStageTest extends FunSuite {

  val recordOne = new Record("aaa", "bbbb")
  val recordTwo = new Record(
    "pushroute",
    "{\"_id\": {\"$oid\": \"5c867a2e6480fd91f0fd9806\"}, \"id\": \"9219258144\", \"type\": \"PushEvent\", \"actor\": {\"id\": 34705957, \"login\": \"trevorvv\", \"display_login\": \"trevorvv\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/trevorvv\", \"avatar_url\": \"https://avatars.githubusercontent.com/u/34705957?\"}, \"repo\": {\"id\": 175021096, \"name\": \"trevorvv/school-werk\", \"url\": \"https://api.github.com/repos/trevorvv/school-werk\"}, \"payload\": {\"push_id\": 3387854980, \"size\": 1, \"distinct_size\": 1, \"ref\": \"refs/heads/master\", \"head\": \"83fdf786c25726fbd00bbbd96250a6d32d886c47\", \"before\": \"efc8ef57f9a246090f19e03b9d4633c1463ca8ca\", \"commits\": [{\"sha\": \"83fdf786c25726fbd00bbbd96250a6d32d886c47\", \"author\": {\"email\": \"vanveentrevor@gmail.com\", \"name\": \"Trevor van Veen\"}, \"message\": \"java final\", \"distinct\": true, \"url\": \"https://api.github.com/repos/trevorvv/school-werk/commits/83fdf786c25726fbd00bbbd96250a6d32d886c47\"}]}, \"public\": true, \"created_at\": \"2019-03-11T15:04:34Z\"}"
  )
  val recordThree = new Record(
    "createroute",
    "{\"_id\": {\"$oid\": \"5c867a296480fd91f0fd9804\"}, \"id\": \"9219255648\", \"type\": \"CreateEvent\", \"actor\": {\"id\": 33662565, \"login\": \"tmhuysen\", \"display_login\": \"tmhuysen\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/tmhuysen\", \"avatar_url\": \"https://avatars.githubusercontent.com/u/33662565?\"}, \"repo\": {\"id\": 155193053, \"name\": \"tmhuysen/gqcp\", \"url\": \"https://api.github.com/repos/tmhuysen/gqcp\"}, \"payload\": {\"ref\": \"feature/error_locations\", \"ref_type\": \"branch\", \"master_branch\": \"develop\", \"description\": \"The Ghent Quantum Chemistry Package for electronic structure calculations\", \"pusher_type\": \"user\"}, \"public\": true, \"created_at\": \"2019-03-11T15:04:17Z\"}"
  )

  test("GHTRecordToEventStage integration test") {
    new PipelineBuilder()
      .append(new GHTTestSource(List(recordOne, recordTwo, recordThree)))
      .append(new GHTAbstractEventStage[PushEvent]("", "pushroute"))
      .append { x: DataStream[PushEvent] =>
        x.addSink(new EventCollectSink[PushEvent])
      }
      .build()
      .startMock()

    assert(EventCollectSink.result.size == 1)
    assert(EventCollectSink.result.get(0).isInstanceOf[PushEvent])

    EventCollectSink.result.clear()
  }

  test("GHTRecordToEventStage integration test 2") {
    new PipelineBuilder()
      .append(new GHTTestSource(List(recordOne, recordTwo, recordThree)))
      .append(new GHTAbstractEventStage[CreateEvent]("", "createroute"))
      .append { x: DataStream[CreateEvent] =>
        x.addSink(new EventCollectSink[CreateEvent])
      }
      .build()
      .startMock()

    assert(EventCollectSink.result.size == 1)
    assert(EventCollectSink.result.get(0).isInstanceOf[CreateEvent])

    EventCollectSink.result.clear()
  }

}

object EventCollectSink {
  val result = new java.util.ArrayList[Event]()
}

class EventCollectSink[T <: Event] extends SinkFunction[T] {
  override def invoke(value: T): Unit = {
    synchronized({
      EventCollectSink.result.add(value)
    })
  }
}
