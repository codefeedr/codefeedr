package org.codefeedr.plugins.rss

import org.codefeedr.pipeline.{NoType, PipelineObject, Source}
import org.apache.flink.streaming.api.scala.{DataStream, _}

class RSSSource(url: String, pollingInterval: Int) extends Source[RSSItem] {

  override def main(): DataStream[RSSItem] = {
    pipeline.environment.addSource(new RSSItemSource(url, pollingInterval))
  }

}