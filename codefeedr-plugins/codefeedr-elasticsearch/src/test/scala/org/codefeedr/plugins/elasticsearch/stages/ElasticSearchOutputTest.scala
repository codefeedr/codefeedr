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

package org.codefeedr.plugins.elasticsearch.stages

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.codefeedr.stages.utilities.StringType
import org.elasticsearch.action.index.IndexRequest
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

class ElasticSearchOutputTest extends FunSuite with MockitoSugar {
  val index = "testIndex"

  test("Should create config object") {
    val stage = new ElasticSearchOutput[StringType](index)

    val config = stage.createConfig()
    assert(config != null)
  }

  test("Should add localhost to connections if none are given") {
    val stage = new ElasticSearchOutput[StringType](index)

    val addresses = stage.createTransportAddresses()

    assert(addresses.size() == 1)
    assert(addresses.get(0).getHostName == "localhost")
    assert(addresses.get(0).getPort == 9200)
  }

  test("Should add configured hosts") {
    val servers = Set("es://example.com:9300", "es://google.com:9200")
    val stage = new ElasticSearchOutput[StringType](index, servers)

    val addresses = stage.createTransportAddresses()

    assert(addresses.size() == 2)
    assert(addresses.get(0).getHostName == "example.com")
    assert(addresses.get(0).getPort == 9300)
    assert(addresses.get(1).getHostName == "google.com")
    assert(addresses.get(1).getPort == 9200)
  }

  test("Should not add servers with no es schema") {
    val servers = Set("es://example.com:9300", "myHost:9200")
    val stage = new ElasticSearchOutput[StringType](index, servers)

    val addresses = stage.createTransportAddresses()

    assert(addresses.size() == 1)
  }

  test("ElasticSearchSinkFunction should properly deserialize") {
    val sink = new ElasticSearchSink[StringType](index)

    assert(sink.serialize(StringType("test")).isInstanceOf[Array[Byte]])
  }

  test("An element should be properly processed.") {
    val sink = new ElasticSearchSink[StringType](index)
    val mockedContext = mock[RuntimeContext]
    val mockedIndex = mock[RequestIndexer]

    sink.process(new StringType(""), mockedContext, mockedIndex)

    verify(mockedIndex).add(any[IndexRequest])
  }

  test("ElasticSearchOutput should properly bind to DataStream") {
    val stage = new ElasticSearchOutput[StringType](index)
    val mockedStream = mock[DataStream[StringType]]

    stage.main(mockedStream)

    verify(mockedStream).addSink(any[ElasticsearchSink[StringType]])
  }

}
