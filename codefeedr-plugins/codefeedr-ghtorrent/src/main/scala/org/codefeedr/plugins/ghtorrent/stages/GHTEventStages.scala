package org.codefeedr.plugins.ghtorrent.stages

import org.codefeedr.plugins.ghtorrent.protocol.GitHub.{
  CommitCommentEvent,
  CreateEvent,
  DeleteEvent,
  PushEvent
}
import org.apache.flink.api.scala._

object GHTEventStages {

  class GHTRecordToCreateEventStage()
      extends GHTRecordToEventStage[CreateEvent]("ght_create",
                                                 "evt.create.insert")
  class GHTRecordToDeleteEventStage()
      extends GHTRecordToEventStage[DeleteEvent]("ght_delete",
                                                 "evt.delete.insert")

  class GHTRecordToPushEventStage()
      extends GHTRecordToEventStage[PushEvent]("ght_push", "evt.push.insert")

  class GHTRecordToCommitCommentEventStage()
      extends GHTRecordToEventStage[CommitCommentEvent](
        "ght_commitcomment",
        "evt.commitcomment.insert")
}
