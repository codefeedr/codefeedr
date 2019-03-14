package org.codefeedr.plugins.ghtorrent.stages

import java.util

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.ghtorrent.GHTTestSource
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.plugins.ghtorrent.protocol.GitHub.Commit
import org.scalatest.FunSuite

class GHTRecordToCommitStageTest extends FunSuite {

  val recordOne = new Record("a", "b")
  val recordTwo = new Record(
    "ent.commits.insert",
    "{\"_id\": {\"$oid\": \"5c867a296480fdaa5cdd769b\"}, \"sha\": \"4dc309fc6bebaacebdcf16f48246721d0d9b4141\", \"node_id\": \"MDY6Q29tbWl0MTc0ODM2MzY5OjRkYzMwOWZjNmJlYmFhY2ViZGNmMTZmNDgyNDY3MjFkMGQ5YjQxNDE=\", \"commit\": {\"author\": {\"name\": \"magnifer\", \"email\": \"31923438+magnifer@users.noreply.github.com\", \"date\": \"2019-03-10T15:24:16Z\"}, \"committer\": {\"name\": \"magnifer\", \"email\": \"31923438+magnifer@users.noreply.github.com\", \"date\": \"2019-03-10T15:24:16Z\"}, \"message\": \"Create com.android.support.appcompat-v7-25.3.1.aar\", \"tree\": {\"sha\": \"84d73bf0a4e35f1888eedd90fb81354c671bae36\", \"url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/git/trees/84d73bf0a4e35f1888eedd90fb81354c671bae36\"}, \"url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/git/commits/4dc309fc6bebaacebdcf16f48246721d0d9b4141\", \"comment_count\": 0, \"verification\": {\"verified\": false, \"reason\": \"unsigned\", \"signature\": null, \"payload\": null}}, \"url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/commits/4dc309fc6bebaacebdcf16f48246721d0d9b4141\", \"html_url\": \"https://github.com/magnifer/FacebookSdkIntegration/commit/4dc309fc6bebaacebdcf16f48246721d0d9b4141\", \"comments_url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/commits/4dc309fc6bebaacebdcf16f48246721d0d9b4141/comments\", \"author\": {\"login\": \"magnifer\", \"id\": 31923438, \"node_id\": \"MDQ6VXNlcjMxOTIzNDM4\", \"avatar_url\": \"https://avatars3.githubusercontent.com/u/31923438?v=4\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/magnifer\", \"html_url\": \"https://github.com/magnifer\", \"followers_url\": \"https://api.github.com/users/magnifer/followers\", \"following_url\": \"https://api.github.com/users/magnifer/following{/other_user}\", \"gists_url\": \"https://api.github.com/users/magnifer/gists{/gist_id}\", \"starred_url\": \"https://api.github.com/users/magnifer/starred{/owner}{/repo}\", \"subscriptions_url\": \"https://api.github.com/users/magnifer/subscriptions\", \"organizations_url\": \"https://api.github.com/users/magnifer/orgs\", \"repos_url\": \"https://api.github.com/users/magnifer/repos\", \"events_url\": \"https://api.github.com/users/magnifer/events{/privacy}\", \"received_events_url\": \"https://api.github.com/users/magnifer/received_events\", \"type\": \"User\", \"site_admin\": false}, \"committer\": {\"login\": \"magnifer\", \"id\": 31923438, \"node_id\": \"MDQ6VXNlcjMxOTIzNDM4\", \"avatar_url\": \"https://avatars3.githubusercontent.com/u/31923438?v=4\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/magnifer\", \"html_url\": \"https://github.com/magnifer\", \"followers_url\": \"https://api.github.com/users/magnifer/followers\", \"following_url\": \"https://api.github.com/users/magnifer/following{/other_user}\", \"gists_url\": \"https://api.github.com/users/magnifer/gists{/gist_id}\", \"starred_url\": \"https://api.github.com/users/magnifer/starred{/owner}{/repo}\", \"subscriptions_url\": \"https://api.github.com/users/magnifer/subscriptions\", \"organizations_url\": \"https://api.github.com/users/magnifer/orgs\", \"repos_url\": \"https://api.github.com/users/magnifer/repos\", \"events_url\": \"https://api.github.com/users/magnifer/events{/privacy}\", \"received_events_url\": \"https://api.github.com/users/magnifer/received_events\", \"type\": \"User\", \"site_admin\": false}, \"parents\": [{\"sha\": \"2211fe3ce3a6e307269f5103de0281efb3a4e125\", \"url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/commits/2211fe3ce3a6e307269f5103de0281efb3a4e125\", \"html_url\": \"https://github.com/magnifer/FacebookSdkIntegration/commit/2211fe3ce3a6e307269f5103de0281efb3a4e125\"}], \"stats\": {\"total\": 0, \"additions\": 0, \"deletions\": 0}, \"files\": [{\"sha\": \"653719b165561561035b78e10640c10c35672ffb\", \"filename\": \"Facebook Sdk Integration/Assets/Plugins/Android/com.android.support.appcompat-v7-25.3.1.aar\", \"status\": \"added\", \"additions\": 0, \"deletions\": 0, \"changes\": 0, \"blob_url\": \"https://github.com/magnifer/FacebookSdkIntegration/blob/4dc309fc6bebaacebdcf16f48246721d0d9b4141/Facebook%20Sdk%20Integration/Assets/Plugins/Android/com.android.support.appcompat-v7-25.3.1.aar\", \"raw_url\": \"https://github.com/magnifer/FacebookSdkIntegration/raw/4dc309fc6bebaacebdcf16f48246721d0d9b4141/Facebook%20Sdk%20Integration/Assets/Plugins/Android/com.android.support.appcompat-v7-25.3.1.aar\", \"contents_url\": \"https://api.github.com/repos/magnifer/FacebookSdkIntegration/contents/Facebook%20Sdk%20Integration/Assets/Plugins/Android/com.android.support.appcompat-v7-25.3.1.aar?ref=4dc309fc6bebaacebdcf16f48246721d0d9b4141\"}], \"etag\": \"a355fa0df2e955c900a96b43ce0caa03\"}"
  )

  test("GHTRecordToCommitStage integration test") {
    new PipelineBuilder()
      .append(new GHTTestSource(List(recordOne, recordTwo)))
      .append(new GHTRecordToCommitStage())
      .append { x: DataStream[Commit] =>
        x.addSink(new CommitCollectSink)
      }
      .build()
      .startMock()

    assert(CommitCollectSink.result.size == 1)
    assert(
      CommitCollectSink.result
        .get(0)
        .sha == "4dc309fc6bebaacebdcf16f48246721d0d9b4141")
  }
}

object CommitCollectSink {
  val result = new util.ArrayList[Commit]()
}

class CommitCollectSink extends SinkFunction[Commit] {
  override def invoke(value: Commit): Unit = {
    synchronized({
      CommitCollectSink.result.add(value)
    })
  }
}
