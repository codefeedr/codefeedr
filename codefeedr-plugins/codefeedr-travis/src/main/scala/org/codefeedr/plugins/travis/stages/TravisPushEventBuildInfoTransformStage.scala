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
 *
 */
package org.codefeedr.plugins.travis.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, _}
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.travis.TravisProtocol.{
  PushEventFromActiveTravisRepo,
  TravisBuild
}
import org.codefeedr.plugins.travis.util.{TravisBuildCollector, TravisService}
import org.codefeedr.stages.TransformStage

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * TransformStage that takes push events from Repositories that are active on Travis and outputs
  * the build information of those push events. If the build is not completed yet it will wait until it is
  * completed
  * @param capacity Limit on how many builds can be requested simultaneously
  */
class TravisPushEventBuildInfoTransformStage(capacity: Int = 100)
    extends TransformStage[PushEventFromActiveTravisRepo, TravisBuild] {

  lazy val travisService: TravisService = new TravisService(
    getContext.pipeline.keyManager)
  def travis: TravisService = travisService

  def transform(source: DataStream[PushEventFromActiveTravisRepo])
    : DataStream[TravisBuild] = {

    AsyncDataStream.unorderedWait(source.map(x => x.pushEventItem),
                                  new TravisBuildStatusRequest(travis),
                                  20,
                                  TimeUnit.MINUTES,
                                  capacity)
  }
}

/**
  * AsyncFunction that takes a push event from a repository that is active on Travis and outputs
  * the build information of that push event. If the build is not completed yet it will wait until it is
  * completed
  * @param travis TravisService used to make requests to Travis
  */
class TravisBuildStatusRequest(travis: TravisService)
    extends AsyncFunction[PushEvent, TravisBuild] {

  implicit val executor: ExecutionContext = ExecutionContext.global

  override def asyncInvoke(input: PushEvent,
                           resultFuture: ResultFuture[TravisBuild]): Unit = {
    // If there are no commits in the push then there will be no build
    if (input.payload.commits.isEmpty) {
      resultFuture.complete(Iterable())
      return
    }

    val splittedSlug = input.repo.name.split('/')
    val futureResultBuild: Future[TravisBuild] =
      new TravisBuildCollector(splittedSlug(0),
                               splittedSlug(1),
                               input.payload.ref.replace("refs/heads/", ""),
                               input.payload.head,
                               input.created_at,
                               travis)
        .requestFinishedBuild()

    futureResultBuild.onComplete {
      case Success(result: TravisBuild) =>
        resultFuture.complete(Iterable(result))
      case Failure(e) =>
        resultFuture.complete(Iterable())
        e.printStackTrace()
    }
  }
}
