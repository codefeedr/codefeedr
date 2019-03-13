package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.stages.InputStage

class GHTorrentInputStage(stageName: String = "ghtorrent_input")
    extends InputStage[Record](Some(stageName)) {

  override def main(context: Context): DataStream[Nothing] = {}
}
