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
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.SimpleSourceStage
import org.scalatest.FunSuite
import org.apache.flink.api.scala._

case class IntType(id: Int)

class StageTest extends FunSuite {

  class BadSourceObject extends Stage[Nothing, StringType] {
    override def transform(
        source: DataStream[Nothing]): DataStream[StringType] = {
      getMainSource()

      null
    }
  }

  class BadSinkObject extends Stage[StringType, Nothing] {
    override def transform(
        source: DataStream[StringType]): DataStream[Nothing] = {
      getSink()

      null
    }
  }

  class StringTypeStage extends Stage[Nothing, StringType] {
    override def transform(
        source: DataStream[Nothing]): DataStream[StringType] = {
      getContext.env.fromCollection(Seq(StringType("a")))
    }
  }

  class IntTypeStage extends Stage[IntType, Nothing] {
    override def transform(source: DataStream[IntType]): DataStream[Nothing] = {
      source.print()

      null
    }
  }

  test("Should throw when getting unknown main source") {
    val pipeline = new PipelineBuilder()
      .disablePipelineVerification()
      .append(new BadSourceObject())
      .build()

    assertThrows[NoSourceException] {
      pipeline.startMock()
    }
  }

  test("Should throw when getting unknown sink") {
    val pipeline = new PipelineBuilder()
      .disablePipelineVerification()
      .append(new BadSinkObject())
      .build()

    assertThrows[NoSinkException] {
      pipeline.startMock()
    }
  }

  test("Setting id attributed propagates") {
    val a = new SimpleSourceStage(Some("testId"))

    assert(a.getContext.stageId == "testId")
  }

  test("Properties should be properly propagated.") {
    val stage = new SimpleSourceStage(Some("ha"))
    val diffStage = new SimpleSourceStage(Some("other"))

    val pipeline = new PipelineBuilder()
      .disablePipelineVerification()
      .setStageProperty(stage, "a", "b")
      .setStageProperty(diffStage, "c", "d")
      .append(stage)
      .build()

    stage.setUp(pipeline)

    assert(stage.getContext.stageProperties.get("a").get == "b")
    assert(stage.getContext.stageProperties.get("c").isEmpty)
  }
}
