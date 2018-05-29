package org.codefeedr.plugins.travis.stages

import java.util

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.github.stages.{GitHubEventToPushEvent, SimpleEventSource}
import org.codefeedr.plugins.travis.TravisProtocol.PushEventFromActiveTravisRepo
import org.codefeedr.plugins.travis.util.TravisService
import org.scalatest.FunSuite
import org.mockito.Mockito._

class TravisFilterActiveReposTransformStageTest extends FunSuite {

  test("Active repos should be filtered") {

    val travis = spy(new TravisService(new StaticKeyManager()))
    val filter = (_: String) => {true}

    doReturn(filter).when(travis).repoIsActiveFilter

    new PipelineBuilder()
      .append(new SimpleEventSource("/sample_events.json"))
      .append(new GitHubEventToPushEvent())
      .append(new TravisFilterActiveReposTransformStage(travis))
      .append { x : DataStream[PushEventFromActiveTravisRepo] =>
        x.addSink(new ActiveRepoPushEventCollectSink)
      }
      .build()
      .startMock()

    assert(ActiveRepoPushEventCollectSink.result.size == 5)
  }
}

object ActiveRepoPushEventCollectSink {
  val result = new util.ArrayList[PushEventFromActiveTravisRepo]() //mutable list
}

class ActiveRepoPushEventCollectSink extends SinkFunction[PushEventFromActiveTravisRepo] {

  override def invoke(value: PushEventFromActiveTravisRepo): Unit = {
    synchronized {
      ActiveRepoPushEventCollectSink.result.add(value)
    }
  }

}
