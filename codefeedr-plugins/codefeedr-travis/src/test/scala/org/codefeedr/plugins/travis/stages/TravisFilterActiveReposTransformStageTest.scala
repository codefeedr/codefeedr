package org.codefeedr.plugins.travis.stages

import java.io.InputStream
import java.util

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.github.GitHubProtocol.Event
import org.codefeedr.plugins.github.requests.EventService
import org.codefeedr.plugins.github.stages.GitHubEventToPushEvent
import org.codefeedr.plugins.travis.TravisProtocol.PushEventFromActiveTravisRepo
import org.codefeedr.plugins.travis.util.TravisService
import org.codefeedr.stages.InputStage
import org.scalatest.FunSuite
import org.mockito.Mockito._

import scala.io.Source

class TravisFilterActiveReposTransformStageTest extends FunSuite {

  test("Active repos should be filtered") {

    val travis = spy(new TravisService(new StaticKeyManager()))
    val filter = (_: String) => {true}
    doReturn(filter).when(travis).repoIsActiveFilter

    val travisFilterActiveReposTransformStage: TravisFilterActiveReposTransformStage =
      spy(new TravisFilterActiveReposTransformStage())
    doReturn(travis).when(travisFilterActiveReposTransformStage).travis

    println(travisFilterActiveReposTransformStage.travis)

    new PipelineBuilder()
      .append(new SimpleEventSource("/sample_events.json"))
      .append(new GitHubEventToPushEvent())
      .append(travisFilterActiveReposTransformStage)
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

class SimpleEventSource(fileName: String) extends InputStage[Event] {

  val stream : InputStream = getClass.getResourceAsStream(fileName)
  val sampleEvents : String = Source.fromInputStream(stream).getLines.mkString

  override def main(): DataStream[Event] = {
    val events = new EventService(false, null)
      .parseEvents(sampleEvents)

    pipeline.environment.fromCollection(events)
  }
}