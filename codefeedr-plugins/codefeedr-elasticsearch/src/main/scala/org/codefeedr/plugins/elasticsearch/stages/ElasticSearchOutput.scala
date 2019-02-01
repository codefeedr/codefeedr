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

import java.net.{InetAddress, InetSocketAddress, URI}
import java.nio.charset.StandardCharsets
import java.util

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.runtime.rest.RestClient
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.{ElasticsearchSink, RestClientFactory}
import org.apache.http.HttpHost
import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.{OutputStage, StageAttributes}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.{Requests, RestClientBuilder}
import org.elasticsearch.common.xcontent.XContentType
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization
import collection.JavaConversions._

import scala.reflect.{ClassTag, Manifest}

/**
  * An output stage that pushes all elements as JSON to an Elastic Search cluster
  *
  * @param index      Elastic Search index to push the data to
  * @param servers    Optional set of server addresses (defaults to es://localhost:9300)
  * @param config     Extra configuration options for the Elastic Search client
  * @param attributes Optional stage attributes
  * @tparam T Input type
  */
class ElasticSearchOutput[T <: Serializable with AnyRef : ClassTag : Manifest](index: String,
                                                                               servers: Set[String] = Set(),
                                                                               config: Map[String, String] = Map(),
                                                                               attributes: StageAttributes = StageAttributes())
  extends OutputStage[T](attributes) with Logging {

  //TODO Add configuration support
  override def main(source: DataStream[T]): Unit = {
    val config = createConfig()
    val transportAddresses = createTransportAddresses()

    val eSinkBuilder = new ElasticsearchSink.Builder[T](transportAddresses, new ElasticSearchSink(index))

    eSinkBuilder.setBulkFlushMaxActions(1)
    source.addSink(eSinkBuilder.build())
  }

  def createConfig(): java.util.HashMap[String, String] = {
    val config = new java.util.HashMap[String, String]
    config
  }

  /**
    * Create elastic search host address list
    *
    * @return List
    */
  def createTransportAddresses(): java.util.ArrayList[HttpHost] = {
    val transportAddresses = new java.util.ArrayList[HttpHost]

    if (servers.isEmpty) {
      logger.info("Transport address set is empty. Using localhost with default port 9300.")
      transportAddresses.add(new HttpHost("localhost", 9300, "http"))
    }

    for (server <- servers) {
      val uri = new URI(server)

      if (uri.getScheme == "es") {
        logger.info(s"Adding transport address $server")
        transportAddresses.add(new HttpHost(uri.getHost, uri.getPort, "http"))
      }
    }

    transportAddresses
  }

}

/**
  * An elastic search sink
  *
  * @param index Index to be used in ElasticSearch
  * @tparam T Type of input
  */
private class ElasticSearchSink[T <: Serializable with AnyRef : ClassTag : Manifest](index: String) extends ElasticsearchSinkFunction[T] {

  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all

  def createIndexRequest(element: T): IndexRequest = {
    val bytes = serialize(element)

    Requests.indexRequest()
      .index(index)
      .`type`("json")
      .source(bytes, XContentType.JSON)
  }

  override def process(element: T, ctx: RuntimeContext, indexer: RequestIndexer): Unit = {
    indexer.add(createIndexRequest(element))
  }

  /**
    * Serialize an element into JSON
    *
    * @param element Element
    * @return JSON bytes
    */
  def serialize(element: T): Array[Byte] = {
    val bytes = Serialization.write[T](element)(formats)

    bytes.getBytes(StandardCharsets.UTF_8)
  }

}
