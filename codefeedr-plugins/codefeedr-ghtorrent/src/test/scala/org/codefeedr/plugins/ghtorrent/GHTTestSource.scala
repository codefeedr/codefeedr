package org.codefeedr.plugins.ghtorrent

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._

class GHTTestSource(listRecords: List[Record]) extends InputStage[Record] {
  override def main(context: Context): DataStream[Record] = {
    context.env.fromCollection(listRecords)
  }
}
