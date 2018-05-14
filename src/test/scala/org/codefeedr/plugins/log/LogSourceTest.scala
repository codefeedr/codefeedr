package org.codefeedr.plugins.log

import java.io.File

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.{Job, PipelineBuilder, PipelineItem}
import org.codefeedr.pipeline.buffer.BufferType
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}

class LogSourceTest extends FunSuite with MockFactory with BeforeAndAfter {

  test("RSSSource end-to-end test") {

    new PipelineBuilder()
      .setBufferType(BufferType.None)
      .append(new ApacheLogFileSource(getClass.getResource("/access.log").getPath))
      .append(new MyLogJob)
      .build()
  }
}

class MyLogJob extends Job[ApacheAccessLogItem] {
  override def main(source: DataStream[ApacheAccessLogItem]): Unit = {
    source.map(x => (x.date, x.request, x.userAgent)).print()
  }
}

