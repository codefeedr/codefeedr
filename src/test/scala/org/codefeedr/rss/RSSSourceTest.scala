package org.codefeedr.rss

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.{Job, PipelineBuilder}
import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.plugins.rss.{RSSItem, RSSSource}
import org.scalamock.scalatest.MockFactory

class RSSSourceTest extends FunSuite with MockFactory with BeforeAndAfter {

  test("RSSSource end-to-end test") {
    new PipelineBuilder()
      .setBufferType(BufferType.None)
      .append(new RSSSource("https://eztv.ag/ezrss.xml", 1000 * 3))
      //      .append(new RSSSource("http://lorem-rss.herokuapp.com/feed?unit=second", 5000))
      .append(new MyRSSJob)
      .build()
      .startMock()
  }
}

class MyRSSJob extends Job[RSSItem] {
  override def main(source: DataStream[RSSItem]): Unit = {
    source.map(x => x.pubDate.toString).print()
  }
}
