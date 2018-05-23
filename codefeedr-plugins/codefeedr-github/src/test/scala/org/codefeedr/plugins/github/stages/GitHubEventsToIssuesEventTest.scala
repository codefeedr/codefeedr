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

import java.util

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.github.GitHubProtocol.{IssuesEvent, PushEvent}
import org.scalatest.FunSuite

class GitHubEventsToIssuesEventTest extends FunSuite{

  test ("GitHubEventsToIssuesEvent integration test") {
    val pipeLine = new PipelineBuilder()
      .append(new SimpleEventSource("/issues_events.json"))
      .append(new GitHubEventToIssuesEvent())
      .append { x : DataStream[IssuesEvent] =>
        x.addSink(new IssuesEventCollectSink)
      }
      .build()
      .startMock()

    //+- there are 2 events
    assert(IssuesEventCollectSink.result.size == 2)
  }

}

object IssuesEventCollectSink {
  val result = new util.ArrayList[IssuesEvent]() //mutable list
}

class IssuesEventCollectSink extends SinkFunction[IssuesEvent] {

  override def invoke(value: IssuesEvent): Unit = {
    synchronized {
      IssuesEventCollectSink.result.add(value)
    }
  }

}
