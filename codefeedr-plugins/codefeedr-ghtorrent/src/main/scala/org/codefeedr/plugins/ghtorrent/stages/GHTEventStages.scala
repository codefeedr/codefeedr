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
}
