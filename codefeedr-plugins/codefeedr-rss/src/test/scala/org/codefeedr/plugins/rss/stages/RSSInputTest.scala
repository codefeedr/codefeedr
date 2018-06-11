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
 */

package org.codefeedr.plugins.rss.stages

import org.apache.flink.api.common.JobID
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.rss.RSSItem
import org.codefeedr.stages.OutputStage
import org.scalatest.FunSuite

//This will be thrown after the print sink received x elements.
final case class JobFinishedException() extends JobExecutionException(new JobID(), "Job is finished.")
class RSSInputTest extends FunSuite {

  test("RSS source pipeline build test") {
    val rssURL = "http://lorem-rss.herokuapp.com/feed?unit=second"
    val source = new RSSInput(rssURL, "EEE, dd MMMM yyyy HH:mm:ss z", 1000)
    val sink = new LimitingSinkPipelineObject(12)

    val pipeline = new PipelineBuilder()
      .append(source)
      .append(sink)
      .build()
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

    println(count)

    if (elements != -1 && count >= elements) {
      println("exception")
      throw JobFinishedException()
    }
  }
}

