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

import org.apache.flink.streaming.api.scala._
import org.codefeedr.buffer.BufferType
import org.codefeedr.pipeline._
import org.codefeedr.plugins.weblogs.HttpdLogItem
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.StringCollectSink
import org.scalatest.{BeforeAndAfter, FunSuite}

class HttpdLogInputTest extends FunSuite with BeforeAndAfter {

  test("LogSource integration test") {
    new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(new HttpdLogInput(getClass.getResource("/access.log").getPath))
      .append(new MyPipelineObject)
      .append { x: DataStream[StringType] =>
        x.addSink(new StringCollectSink)
      }
      .build()
      .startMock()

    val res = StringCollectSink.result

    assert(res.contains("HttpdLogItem(46.72.177.4,2015-12-12T18:31:08,POST,/administrator/index.php,HTTP/1.1,200,4494,\"http://almhuette-raith.at/administrator/\",\"Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0\")"))
  }

}

class MyPipelineObject extends PipelineObject[HttpdLogItem, StringType] {
  override def transform(source: DataStream[HttpdLogItem]): DataStream[StringType] = {
    source.map(x => StringType(x.toString))
  }
}

