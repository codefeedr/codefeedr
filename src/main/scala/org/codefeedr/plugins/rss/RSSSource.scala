package org.codefeedr.plugins.rss

import org.codefeedr.pipeline.{NoType, PipelineObject}
import org.apache.flink.streaming.api.scala.{DataStream, _}


class RSSSource(url: String, pollingInterval: Int) extends PipelineObject[NoType, RSSItem] {
  override def transform(source: DataStream[NoType]): DataStream[RSSItem] = {
    pipeline.environment.addSource(new RSSItemSource(url, pollingInterval))
  }
}

object RSSTest {
  def main(args: Array[String]): Unit = {


  }
}