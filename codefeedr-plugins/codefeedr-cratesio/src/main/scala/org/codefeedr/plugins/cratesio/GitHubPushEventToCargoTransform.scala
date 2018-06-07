package org.codefeedr.plugins.cratesio

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.cratesio.CargoProtocol.CrateInfo
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.apache.flink.api.scala._
import org.codefeedr.stages.TransformStage

class GitHubPushEventToCargoTransform(repoName : String = "rust-lang/crates.io-index") extends TransformStage[PushEvent, CrateInfo] {

  /**
    * Transfrom PushEvents into CrateInfo.
    *
    * @param source a push_event stream.
    * @return a stream of crateinfos.
    */
  override def transform(source: DataStream[PushEvent]): DataStream[CrateInfo] = {

    val service = new CratesIOAPIService()

    source.
      filter(e => e.repo.name == repoName).
      flatMap(e => e.payload.commits.map(c => service.extractCrateFromCommitMsg(c.message))).
      filter (t => t.isSuccess).
      map(crate => service.crateInfo(crate.get))
  }
}
