package org.codefeedr

import org.apache.flink.api.scala._
import org.codefeedr.pipeline._
import org.codefeedr.plugins.rss._

class MyJob extends Job[RSSItem] {
  override def main(): Unit = {
    getSource
      .map { item => (item.title, 1) }
      .keyBy(0)
      .sum(1)
      .print()
  }
}

object Main {

  def main(args: Array[String]): Unit = {

    // Create pipeline
    val builder = new PipelineBuilder()
    builder.setBufferType(BufferType.Fake)

    val source = new RSSSource("")
    val job = new MyJob()
//    builder.pipe(source, job)
    builder.add(source)
    builder.add(job)

    val pipeline = builder.build()

    // Run
    pipeline.start(args)
  }
}