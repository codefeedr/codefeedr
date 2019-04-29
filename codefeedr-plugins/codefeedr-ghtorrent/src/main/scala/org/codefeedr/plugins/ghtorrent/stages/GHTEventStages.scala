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

/** Wrapper for all event classes. **/
object GHTEventStages {

  class GHTCreateEventStage(stageName: String = "ght_create",
                            sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[CreateEvent](stageName,
                                                 "evt.create.insert",
                                                 sideOutput)

  class GHTDeleteEventStage(stageName: String = "ght_delete",
                            sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[DeleteEvent](stageName,
                                                 "evt.delete.insert",
                                                 sideOutput)

  class GHTPushEventStage(stageName: String = "ght_push",
                          sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[PushEvent](stageName,
                                               "evt.push.insert",
                                               sideOutput)

  class GHTCommitCommentEventStage(stageName: String = "ght_commitcomment",
                                   sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[CommitCommentEvent](
        stageName,
        "evt.commitcomment.insert",
        sideOutput)

  class GHTPullRequestEventStage(stageName: String = "ght_pullrequest",
                                 sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[PullRequestEvent](stageName,
                                                      "evt.pullrequest.insert",
                                                      sideOutput)

  class GHTDeploymentEventStage(stageName: String = "ght_deployment",
                                sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[DeploymentEvent](stageName,
                                                     "evt.deployment.insert",
                                                     sideOutput)

  class GHTDeploymentStatusEventStage(
      stageName: String = "ght_deploymentstatus",
      sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[DeploymentStatusEvent](
        stageName,
        "evt.deploymentstatus.insert",
        sideOutput)

  class GHTForkEventStage(stageName: String = "ght_fork",
                          sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[ForkEvent](stageName,
                                               "evt.fork.insert",
                                               sideOutput)

  class GHTGolumEventStage(stageName: String = "ght_gollum",
                           sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[GollumEvent](stageName,
                                                 "evt.gollum.insert",
                                                 sideOutput)

  class GHTIssuesEventStage(stageName: String = "ght_issues",
                            sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[IssuesEvent](stageName,
                                                 "evt.issues.insert",
                                                 sideOutput)

  class GHTIssueCommentEventStage(stageName: String = "ght_issuecomment",
                                  sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[IssueCommentEvent](
        stageName,
        "evt.issuecomment.insert",
        sideOutput)

  class GHTMemberEventStage(stageName: String = "ght_member",
                            sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[MemberEvent](stageName,
                                                 "evt.member.insert",
                                                 sideOutput)

  class GHTMemberShipEventStage(stageName: String = "ght_membership",
                                sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[MemberShipEvent](stageName,
                                                     "evt.membership.insert",
                                                     sideOutput)

  class GHTPublicEventStage(stageName: String = "ght_public",
                            sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[PublicEvent](stageName,
                                                 "evt.public.insert",
                                                 sideOutput)

  class GHTPageBuildEventStage(stageName: String = "ght_pagebuild",
                               sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[PageBuildEvent](stageName,
                                                    "evt.pagebuild.insert",
                                                    sideOutput)

  class GHTPullRequestReviewCommentEventStage(
      stageName: String = "ght_pullrequestreviewcomment",
      sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[PullRequestReviewCommentEvent](
        stageName,
        "evt.pullrequestreviewcomment.insert",
        sideOutput)

  class GHTReleaseEventStage(stageName: String = "ght_release",
                             sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[ReleaseEvent](stageName,
                                                  "evt.release.insert",
                                                  sideOutput)

  class GHTRepositoryEventStage(stageName: String = "ght_repository",
                                sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[RepositoryEvent](stageName,
                                                     "evt.repository.insert",
                                                     sideOutput)

  class GHTTeamAddEventStage(stageName: String = "ght_teamadd",
                             sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[TeamAddEvent](stageName,
                                                  "evt.teamadd.insert",
                                                  sideOutput)

  class GHTWatchEventStage(stageName: String = "ght_watch",
                           sideOutput: SideOutput = SideOutput())
      extends GHTAbstractEventStage[WatchEvent](stageName,
                                                "evt.watch.insert",
                                                sideOutput)

}
