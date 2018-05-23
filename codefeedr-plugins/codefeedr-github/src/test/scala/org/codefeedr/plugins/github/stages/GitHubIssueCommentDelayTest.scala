/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.plugins.github.stages

import java.io.InputStream
import java.util

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.github.requests.EventService
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import scala.io.Source

class GitHubIssueCommentDelayTest extends FunSuite with MockFactory {
  val stream : InputStream = getClass.getResourceAsStream("/issuecommentdelay_events.json")
  val sampleEvents : String = Source.fromInputStream(stream).getLines.mkString

  test ("GitHubIssueCommentDelay integration test") {
    val e = StreamExecutionEnvironment.getExecutionEnvironment
    e.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val events = new EventService(false, null)

    val eventSource = e.fromCollection(events.parseEvents(sampleEvents))
    val eventSource2 = e.fromCollection(events.parseEvents(sampleEvents))

    val transformCommentEvent = new GitHubEventToIssueCommentEvent().transform(eventSource)
    val transformIssuesEvent = new GitHubEventToIssuesEvent().transform(eventSource2)

    val delay = spy(new GitHubIssueCommentDelay())

    //ignore set event time
    doNothing()
      .when(delay)
      .setEventTime()

    val join = delay.transform(transformIssuesEvent, transformCommentEvent)

    join.addSink(new IssueCommentDelayCollectSink)

    e.execute()

    assert(IssueCommentDelayCollectSink.result.size == 1)
    assert(IssueCommentDelayCollectSink.result.get(0).id == 1000.0)
    assert(IssueCommentDelayCollectSink.result.get(0).secondsDelay == 599)
  }
}

object IssueCommentDelayCollectSink {
  val result = new util.ArrayList[IssueOpenedReply]() //mutable list
}

class IssueCommentDelayCollectSink extends SinkFunction[IssueOpenedReply] {

  override def invoke(value: IssueOpenedReply): Unit = {
    synchronized {
      IssueCommentDelayCollectSink.result.add(value)
    }
  }

}
