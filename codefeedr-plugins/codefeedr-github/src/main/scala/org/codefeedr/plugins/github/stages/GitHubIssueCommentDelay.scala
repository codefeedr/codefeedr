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

import java.util.Date

import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.codefeedr.pipeline.PipelineItem
import org.codefeedr.plugins.github.GitHubProtocol.{IssueCommentEvent, IssuesEvent}
import org.apache.flink.api.scala._
import org.codefeedr.stages.TransformStage2

case class IssueOpenedReply(id: Double,
                            secondsDelay: Long) extends PipelineItem

case class SimpleIssue(issueId: Double,
                       created_at: Date)

class GitHubIssueCommentDelay extends TransformStage2[IssuesEvent, IssueCommentEvent, IssueOpenedReply]{
  override def transform(source: DataStream[IssuesEvent], secondSource: DataStream[IssueCommentEvent]): DataStream[IssueOpenedReply] = {
    setEventTime() //sets correct event time

    val secondSourceTimeStamps = secondSource
      .assignAscendingTimestamps(_.created_at.getTime)

    source
      .assignAscendingTimestamps(_.created_at.getTime)
      .filter(_.payload.action == "opened")
      .map(x => SimpleIssue(x.payload.issue.id, x.payload.issue.created_at.get))
      .join(secondSourceTimeStamps)
      .where(_.issueId)
      .equalTo(_.payload.issue.id)
      .window(EventTimeSessionWindows.withGap(Time.minutes(30)))
      .apply(new JoinFunction[SimpleIssue, IssueCommentEvent, IssueOpenedReply] {
        override def join(first: SimpleIssue, second: IssueCommentEvent): IssueOpenedReply = {
          val deltaSeconds = (second.payload.comment.created_at.get.getTime - first.created_at.getTime) / 1000

          IssueOpenedReply(first.issueId, deltaSeconds)
        }
      })
  }

  /**
    * Sets the correct event time.
    */
  def setEventTime() = pipeline.environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)




}
