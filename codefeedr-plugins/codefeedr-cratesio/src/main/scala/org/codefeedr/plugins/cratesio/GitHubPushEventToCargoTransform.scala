package org.codefeedr.plugins.cratesio

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.cratesio.CargoProtocol.CrateInfo
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.apache.flink.api.scala._
import org.codefeedr.stages.TransformStage

class GitHubPushEventToCargoTransform extends TransformStage[PushEvent, CrateInfo] {

  override def transform(source: DataStream[PushEvent]): DataStream[CrateInfo] = {

    val service = new CratesIOAPIService()

    source.
      filter(e => e.repo.name == "rust-lang/crates.io-index").
      flatMap(e => e.payload.commits.map(c => service.extactCrateFromCommitMsg(c.message))).
      filter (t => t.isSuccess).
      map(crate => service.crateInfo(crate.get))
  }
}
