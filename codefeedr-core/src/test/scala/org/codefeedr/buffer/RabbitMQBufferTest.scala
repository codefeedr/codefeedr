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

package org.codefeedr.buffer

import org.apache.flink.api.scala._
import org.apache.flink.runtime.client.JobExecutionException
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.stages.utilities.{StringInput, StringType}
import org.codefeedr.testUtils.CodeHitException
import org.scalatest.{BeforeAndAfter, FunSuite}

class RabbitMQBufferTest extends FunSuite with BeforeAndAfter {

  test("Should throw when using address of non-existing server") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.RabbitMQ)
      .setBufferProperty(RabbitMQBuffer.URI, "amqp://localhost:10000")
      .append(new StringInput("hello world"))
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }
  }

  test("Should pass through messages between stages") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.RabbitMQ)
      .append(new StringInput("hello world"))
      .append { x: DataStream[StringType] =>
        x.map { v => {
          if (v.value == "hello") throw new CodeHitException
        }}
      }
      .build()

    assertThrows[JobExecutionException] {
      pipeline.startLocal()
    }
  }

}
