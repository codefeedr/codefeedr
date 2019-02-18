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
package org.codefeedr.plugins.github.stages

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.github.GitHubProtocol._
import org.codefeedr.stages.TransformStage
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse

/**
  * Transform stage which reads from EventsInput and filters to IssueCommentEvent.
  */
class GitHubEventToIssueCommentEvent
    extends TransformStage[Event, IssueCommentEvent] {

  /**
    * Filter and parses IssueCommentEvent from GitHub Event stream.
    */
  override def transform(
      source: DataStream[Event]): DataStream[IssueCommentEvent] = {
    source
      .filter(_.eventType == "IssueCommentEvent")
      .map { x =>
        implicit val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all

        val issuePayload = parse(x.payload).extract[IssueCommentPayload]
        IssueCommentEvent(x.id,
                          x.eventType,
                          x.actor,
                          x.repo,
                          x.organization,
                          issuePayload,
                          x.public,
                          x.created_at)
      }
  }

}
