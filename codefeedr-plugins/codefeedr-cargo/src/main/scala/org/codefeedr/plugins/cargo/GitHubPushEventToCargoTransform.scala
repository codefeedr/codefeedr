package org.codefeedr.plugins.cargo

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.TransformStage
import org.codefeedr.plugins.cargo.CargoProtocol.CrateInfo
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.apache.flink.api.scala._

class GitHubPushEventToCargoTransform extends TransformStage[PushEvent, CrateInfo] {

  override def transform(source: DataStream[PushEvent]): DataStream[CrateInfo] = {

    source.
      filter(e => e.repo.name == "rust-lang/crates.io-index").
      flatMap(e => e.payload.commits.map(c => CargoService.extactCrateFromCommitMsg(c.message))).
      map(crate => CargoService.crateInfo(crate))
  }
}
