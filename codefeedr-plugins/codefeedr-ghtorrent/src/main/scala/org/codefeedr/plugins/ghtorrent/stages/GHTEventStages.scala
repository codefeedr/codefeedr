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
package org.codefeedr.plugins.ghtorrent.stages

import org.codefeedr.plugins.ghtorrent.protocol.GitHub._
import org.apache.flink.api.scala._

object GHTEventStages {

  class GHTRecordToCreateEventStage(stageName: String = "ght_create",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[CreateEvent](stageName,
                                                 "evt.create.insert",
                                                 sideOutput)

  class GHTRecordToDeleteEventStage(stageName: String = "ght_delete",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeleteEvent](stageName,
                                                 "evt.delete.insert",
                                                 sideOutput)

  class GHTRecordToPushEventStage(stageName: String = "ght_push",
                                  sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PushEvent](stageName,
                                               "evt.push.insert",
                                               sideOutput)

  class GHTRecordToCommitCommentEventStage(
      stageName: String = "ght_commitcomment",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[CommitCommentEvent](
        stageName,
        "evt.commitcomment.insert",
        sideOutput)

  class GHTRecordToPullRequestEventStage(stageName: String = "ght_pullrequest",
                                         sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PullRequestEvent](stageName,
                                                      "evt.pullrequest.insert",
                                                      sideOutput)

  class GHTRecordToDeploymentEventStage(stageName: String = "ght_deployment",
                                        sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeploymentEvent](stageName,
                                                     "evt.deployment.insert",
                                                     sideOutput)

  class GHTRecordToDeploymentStatusEventStage(
      stageName: String = "ght_deploymentstatus",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeploymentStatusEvent](
        stageName,
        "evt.deploymentstatus.insert",
        sideOutput)

  class GHTRecordToForkEventStage(stageName: String = "ght_fork",
                                  sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[ForkEvent](stageName,
                                               "evt.fork.insert",
                                               sideOutput)

  class GHTRecordToGolumEventStage(stageName: String = "ght_gollum",
                                   sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[GollumEvent](stageName,
                                                 "evt.gollum.insert",
                                                 sideOutput)

  class GHTRecordToIssuesEventStage(stageName: String = "ght_issues",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[IssuesEvent](stageName,
                                                 "evt.issues.insert",
                                                 sideOutput)

  class GHTRecordToIssueCommentEventStage(
      stageName: String = "ght_issuecomment",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[IssueCommentEvent](
        stageName,
        "evt.issuecomment.insert",
        sideOutput)

  class GHTRecordToMemberEventStage(stageName: String = "ght_member",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[MemberEvent](stageName,
                                                 "evt.member.insert",
                                                 sideOutput)

  class GHTRecordToMemberShipEventStage(stageName: String = "ght_membership",
                                        sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[MemberShipEvent](stageName,
                                                     "evt.membership.insert",
                                                     sideOutput)

  class GHTRecordToPublicEventStage(stageName: String = "ght_public",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PublicEvent](stageName,
                                                 "evt.public.insert",
                                                 sideOutput)

  class GHTRecordToPageBuildEventStage(stageName: String = "ght_pagebuild",
                                       sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PageBuildEvent](stageName,
                                                    "evt.pagebuild.insert",
                                                    sideOutput)

  class GHTRecordToPullRequestReviewCommentEventStage(
      stageName: String = "ght_pullrequestreviewcomment",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PullRequestReviewCommentEvent](
        stageName,
        "evt.pullrequestreviewcomment.insert",
        sideOutput)

  class GHTRecordToReleaseEventStage(stageName: String = "ght_release",
                                     sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[ReleaseEvent](stageName,
                                                  "evt.release.insert",
                                                  sideOutput)

  class GHTRecordToRepositoryEventStage(stageName: String = "ght_repository",
                                        sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[RepositoryEvent](stageName,
                                                     "evt.repository.insert",
                                                     sideOutput)

  class GHTRecordToTeamAddEventStage(stageName: String = "ght_teamadd",
                                     sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[TeamAddEvent](stageName,
                                                  "evt.teamadd.insert",
                                                  sideOutput)

  class GHTRecordToWatchEventStage(stageName: String = "ght_watch",
                                   sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[WatchEvent](stageName,
                                                "evt.watch.insert",
                                                sideOutput)

}
