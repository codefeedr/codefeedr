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

import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.travis.TravisProtocol.PushEventFromActiveTravisRepo
import org.codefeedr.plugins.travis.util.TravisService
import org.codefeedr.stages.TransformStage

/**
  * TransformStage that takes push events from github and filters the push events from repositories
  * that are active on Travis
  */
class TravisFilterActiveReposTransformStage()
    extends TransformStage[PushEvent, PushEventFromActiveTravisRepo] {

  lazy val travisService: TravisService = new TravisService(
    getContext.pipeline.keyManager)
  def travis: TravisService = travisService

  override def transform(source: DataStream[PushEvent])
    : DataStream[PushEventFromActiveTravisRepo] = {

    val filter = travis.repoIsActiveFilter

    source
      .filter(x => filter(x.repo.name))
      .map(PushEventFromActiveTravisRepo)
  }
}
