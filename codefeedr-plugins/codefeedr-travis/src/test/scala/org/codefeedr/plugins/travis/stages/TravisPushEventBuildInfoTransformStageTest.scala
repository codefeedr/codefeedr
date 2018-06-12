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
package org.codefeedr.plugins.travis.stages

import org.apache.flink.streaming.api.scala.async.ResultFuture
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds}
import org.codefeedr.plugins.travis.util.TravisService
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite

import scala.io.Source

class TravisPushEventBuildInfoTransformStageTest extends FunSuite with MockFactory {

  test("Travis build status request gives a travis build") {
    implicit val formats: Formats = DefaultFormats ++ JavaTimeSerializers.all
    val pushEvent =
      parse(Source.fromInputStream(getClass.getResourceAsStream("/single_push_event.json")).mkString)
      .extract[PushEvent]

    val travisBuilds =
      parse(Source.fromInputStream(getClass.getResourceAsStream("/single_push_event_build.json")).mkString)
      .extract[TravisBuilds]

    val travis = spy(new TravisService(new StaticKeyManager))
    doReturn(travisBuilds)
      .when(travis)
      .getTravisBuilds(any(classOf[String]), any(classOf[String]), any(classOf[String]), any(classOf[Int]), any(classOf[Int]))

    val travisBuildStatusRequest = new TravisBuildStatusRequest(travis)

    var iterable: Iterable[TravisBuild] = null

    val resultFuture: ResultFuture[TravisBuild] = new ResultFuture[TravisBuild] {
      override def complete(result: Iterable[TravisBuild]): Unit = {
        iterable = result
      }

      override def completeExceptionally(throwable: Throwable): Unit = {fail()}
    }
    travisBuildStatusRequest.asyncInvoke(pushEvent, resultFuture)

    while(iterable == null) {
      Thread.sleep(100)
      println("waiting for iterable")
    }

    assert(iterable.head.state == "passed")
  }

}