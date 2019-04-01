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

  class GHTCreateEventStage(stageName: String = "ght_create",
                            sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[CreateEvent](stageName,
                                                 "evt.create.insert",
                                                 sideOutput)

  class GHTDeleteEventStage(stageName: String = "ght_delete",
                            sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeleteEvent](stageName,
                                                 "evt.delete.insert",
                                                 sideOutput)

  class GHTPushEventStage(stageName: String = "ght_push",
                          sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PushEvent](stageName,
                                               "evt.push.insert",
                                               sideOutput)

  class GHTCommitCommentEventStage(stageName: String = "ght_commitcomment",
                                   sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[CommitCommentEvent](
        stageName,
        "evt.commitcomment.insert",
        sideOutput)

  class GHTPullRequestEventStage(stageName: String = "ght_pullrequest",
                                 sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PullRequestEvent](stageName,
                                                      "evt.pullrequest.insert",
                                                      sideOutput)

  class GHTDeploymentEventStage(stageName: String = "ght_deployment",
                                sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeploymentEvent](stageName,
                                                     "evt.deployment.insert",
                                                     sideOutput)

  class GHTDeploymentStatusEventStage(
      stageName: String = "ght_deploymentstatus",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[DeploymentStatusEvent](
        stageName,
        "evt.deploymentstatus.insert",
        sideOutput)

  class GHTForkEventStage(stageName: String = "ght_fork",
                          sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[ForkEvent](stageName,
                                               "evt.fork.insert",
                                               sideOutput)

  class GHTGolumEventStage(stageName: String = "ght_gollum",
                           sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[GollumEvent](stageName,
                                                 "evt.gollum.insert",
                                                 sideOutput)

  class GHTIssuesEventStage(stageName: String = "ght_issues",
                            sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[IssuesEvent](stageName,
                                                 "evt.issues.insert",
                                                 sideOutput)

  class GHTIssueCommentEventStage(stageName: String = "ght_issuecomment",
                                  sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[IssueCommentEvent](
        stageName,
        "evt.issuecomment.insert",
        sideOutput)

  class GHTMemberEventStage(stageName: String = "ght_member",
                            sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[MemberEvent](stageName,
                                                 "evt.member.insert",
                                                 sideOutput)

  class GHTMemberShipEventStage(stageName: String = "ght_membership",
                                sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[MemberShipEvent](stageName,
                                                     "evt.membership.insert",
                                                     sideOutput)

  class GHTPublicEventStage(stageName: String = "ght_public",
                            sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PublicEvent](stageName,
                                                 "evt.public.insert",
                                                 sideOutput)

  class GHTPageBuildEventStage(stageName: String = "ght_pagebuild",
                               sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PageBuildEvent](stageName,
                                                    "evt.pagebuild.insert",
                                                    sideOutput)

  class GHTPullRequestReviewCommentEventStage(
      stageName: String = "ght_pullrequestreviewcomment",
      sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[PullRequestReviewCommentEvent](
        stageName,
        "evt.pullrequestreviewcomment.insert",
        sideOutput)

  class GHRecordToReleaseEventStage(stageName: String = "ght_release",
                                    sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[ReleaseEvent](stageName,
                                                  "evt.release.insert",
                                                  sideOutput)

  class GHTRepositoryEventStage(stageName: String = "ght_repository",
                                sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[RepositoryEvent](stageName,
                                                     "evt.repository.insert",
                                                     sideOutput)

  class GHTTeamAddEventStage(stageName: String = "ght_teamadd",
                             sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[TeamAddEvent](stageName,
                                                  "evt.teamadd.insert",
                                                  sideOutput)

  class GHTWatchEventStage(stageName: String = "ght_watch",
                           sideOutput: SideOutput = SideOutput())
      extends GHTRecordToEventStage[WatchEvent](stageName,
                                                "evt.watch.insert",
                                                sideOutput)

}
