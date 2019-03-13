package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.plugins.ghtorrent.util.GHTorrentRMQSource
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._

class GHTorrentInputStage(stageName: String = "ghtorrent_input",
                          username: String)
    extends InputStage[Record](Some(stageName)) {

  override def main(context: Context): DataStream[Record] = {
    context.env.addSource(new GHTorrentRMQSource(username)).map { x =>
      val splitEl = x.split("#", 2)
      val routingKey = splitEl(0)
      val record = splitEl(1)

      Record(routingKey, record)
    }
  }
}
