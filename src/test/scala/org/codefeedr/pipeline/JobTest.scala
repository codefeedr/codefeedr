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

package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.plugins.StringSource
import org.codefeedr.testUtils.{CodeHitException, StringType}
import org.scalatest.FunSuite

class JobTest extends FunSuite {

  class MyJob1 extends OutputStage[StringType] {
    override def main(source: DataStream[StringType]): Unit = {
      if (source == null) {
        throw CodeHitException()
      }
    }
  }

  class MyJob2 extends OutputStage2[StringType, StringType] {
    override def main(source: DataStream[StringType], secondSource: DataStream[StringType]): Unit = {
      if (source == null || secondSource == null) {
        throw CodeHitException()
      }
    }
  }

  class MyJob3 extends OutputStage3[StringType, StringType, StringType] {
    override def main(source: DataStream[StringType], secondSource: DataStream[StringType], thirdSource: DataStream[StringType]): Unit = {
      if (source == null || secondSource == null || thirdSource == null) {
        throw CodeHitException()
      }
    }
  }

  class MyJob4 extends OutputStage4[StringType, StringType, StringType, StringType] {
    override def main(source: DataStream[StringType], secondSource: DataStream[StringType], thirdSource: DataStream[StringType], fourthSource: DataStream[StringType]): Unit = {
      if (source == null || secondSource == null || thirdSource == null || fourthSource == null) {
        throw CodeHitException()
      }
    }
  }

  test("Job with single source") {
    val pipeline = new PipelineBuilder()
      .append(new StringSource())
      .append(new MyJob1())
      .build()

    pipeline.startMock()
  }

  test("Job with two sources") {
    val a = new StringSource()
    val b = new StringSource()
    val job = new MyJob2()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b)
      .build()

    pipeline.startLocal()
  }

  test("Job with three sources") {
    val a = new StringSource()
    val b = new StringSource()
    val c = new StringSource()
    val job = new MyJob3()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c)
      .build()

    pipeline.startLocal()
  }

  test("Job with four sources") {
    val a = new StringSource()
    val b = new StringSource()
    val c = new StringSource()
    val d = new StringSource()
    val job = new MyJob4()

    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .addParents(job, a :+ b :+ c :+ d)
      .build()

    pipeline.startLocal()
  }

}
