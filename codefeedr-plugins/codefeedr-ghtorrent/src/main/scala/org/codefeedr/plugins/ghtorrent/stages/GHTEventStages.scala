package org.codefeedr.plugins.ghtorrent.stages

import org.codefeedr.plugins.ghtorrent.protocol.GitHub._
import org.apache.flink.api.scala._

object GHTEventStages {

  class GHTRecordToCreateEventStage(stageName: String = "ght_create")
      extends GHTRecordToEventStage[CreateEvent](stageName, "evt.create.insert")
  class GHTRecordToDeleteEventStage(stageName: String = "ght_delete")
      extends GHTRecordToEventStage[DeleteEvent](stageName, "evt.delete.insert")

  class GHTRecordToPushEventStage(stageName: String = "ght_push")
      extends GHTRecordToEventStage[PushEvent](stageName, "evt.push.insert")

  class GHTRecordToCommitCommentEventStage(
      stageName: String = "ght_commitcomment")
      extends GHTRecordToEventStage[CommitCommentEvent](
        stageName,
        "evt.commitcomment.insert")

  class GHTRecordToPullRequestEventStage(stageName: String = "ght_pullrequest")
      extends GHTRecordToEventStage[PullRequestEvent](stageName,
                                                      "evt.pullrequest.insert")

  class GHTRecordToDeploymentEventStage(stageName: String = "ght_deployment")
      extends GHTRecordToEventStage[DeploymentEvent](stageName,
                                                     "evt.deployment.insert")

  class GHTRecordToDeploymentStatusEventStage(
      stageName: String = "ght_deploymentstatus")
      extends GHTRecordToEventStage[DeploymentStatusEvent](
        stageName,
        "evt.deploymentstatus.insert")

  class GHTRecordToForkEventStage(stageName: String = "ght_fork")
      extends GHTRecordToEventStage[ForkEvent](stageName, "evt.fork.insert")

  class GHTRecordToGolumEventStage(stageName: String = "ght_gollum")
      extends GHTRecordToEventStage[GollumEvent](stageName, "evt.gollum.insert")

  class GHTRecordToIssuesEvent(stageName: String = "ght_issues")
      extends GHTRecordToEventStage[IssuesEvent](stageName, "evt.issues.insert")

  class GHTRecordToIssueCommentEvent(stageName: String = "ght_issuecomment")
      extends GHTRecordToEventStage[IssuesEvent](stageName,
                                                 "evt.issuecomment.insert")
  class GHTRecordToMemberEvent(stageName: String = "ght_member")
      extends GHTRecordToEventStage[MemberEvent](stageName, "evt.member.insert")

  class GHTRecordToMemberShipEvent(stageName: String = "ght_membership")
      extends GHTRecordToEventStage[MemberShipEvent](stageName,
                                                     "evt.membership.insert")
}
