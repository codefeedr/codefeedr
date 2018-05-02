package org.codefeedr.plugins.rss

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.{NoType, Pipeline, PipelineObject}

class RSSSource(url: String) extends PipelineObject[NoType, RSSItem] {
  override def transform(source: DataStream[NoType]): DataStream[RSSItem] = {
    null
  }
}