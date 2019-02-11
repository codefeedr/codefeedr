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

package org.codefeedr.plugins.weblogs.stages

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala._
import org.codefeedr.buffer.BufferType
import org.codefeedr.pipeline._
import org.codefeedr.plugins.weblogs.HttpdLogItem
import org.scalatest.{BeforeAndAfter, FunSuite}

class HttpdLogInputTest extends FunSuite with BeforeAndAfter {

  test("LogSource integration test") {
    new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(new HttpdLogInput(getClass.getResource("/access.log").getPath))
      .append { x: DataStream[HttpdLogItem] =>
        x.addSink(new ActiveRepoPushEventCollectSink)
      }
      .build()
      .startMock()

    val res = ActiveRepoPushEventCollectSink.result

    assert(res.size == 11)

    assert(res.count(x => x.method == "POST") == 5)
    assert(res.count(x => x.method == "GET") == 6)
    
    assert(res.count { x =>
      x.ip == "109.184.11.34" &&
        x.method == "POST" &&
        x.path == "/administrator/index.php" &&
        x.version == "HTTP/1.1" &&
        x.status == 200 &&
        x.amountOfBytes == 4494 &&
        x.referer == "\"http://almhuette-raith.at/administrator/\"" &&
        x.userAgent == "\"Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0\""
    } == 1)
  }

}

object ActiveRepoPushEventCollectSink {
  var result: List[HttpdLogItem] = List()

  def add(item: HttpdLogItem): Unit = this.synchronized {
    result :+= item
  }
}

class ActiveRepoPushEventCollectSink extends SinkFunction[HttpdLogItem] {

  override def invoke(value: HttpdLogItem): Unit = {
    ActiveRepoPushEventCollectSink.add(value)
  }
}

