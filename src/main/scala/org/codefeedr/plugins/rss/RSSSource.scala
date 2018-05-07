package org.codefeedr.plugins.rss

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.{NoType, PipelineObject}

class RSSSource(url: String)(implicit val recordFrom: FromRecord[RSSItem]) extends PipelineObject[NoType, RSSItem] {
  override def transform(source: DataStream[NoType]): DataStream[RSSItem] = {
    null
  }
}