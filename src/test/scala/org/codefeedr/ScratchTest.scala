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

package org.codefeedr

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.serialization.Serializer
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.pipeline.{Job2, PipelineBuilder}
import org.codefeedr.plugins.{StringSource, StringType}
import org.scalatest.FunSuite


class MyTestJoiner extends Job2[StringType, StringType] {
  override def main(source: DataStream[StringType], secondSource: DataStream[StringType]): Unit = {
    secondSource.print()
  }
}

class ScratchTest extends FunSuite {

//  test("Test odd") {
//    val nodeA = new StringSource()
//    val nodeB = new StringSource()
//    val nodeC = new MyTestJoiner()
//
//    new PipelineBuilder()
////      .edge(nodeA, nodeC)
////      .edge(nodeB, nodeC)
//      .setParents(nodeC, List(nodeA, nodeB))
//
//      .setBufferType(BufferType.Kafka)
//      .setBufferProperty(KafkaBuffer.SERIALIZER, Serializer.JSON)
//      .setBufferProperty(KafkaBuffer.HOST, "localhost:9092")
//      .build()
//      .startLocal()
//  }
}
