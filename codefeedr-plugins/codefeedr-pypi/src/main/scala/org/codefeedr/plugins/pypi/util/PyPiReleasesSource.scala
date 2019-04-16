package org.codefeedr.plugins.pypi.util

import java.net.URL

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.state.{
  ListState,
  ListStateDescriptor,
  StateTtlConfig
}
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{
  RichSourceFunction,
  SourceFunction
}
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiRelease

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import java.util.concurrent._

class PyPiReleasesSource(waitTime: Long = 1000)
    extends RichSourceFunction[PyPiRelease] {

  val feedUrl = new URL("https://pypi.org/rss/updates.xml")
  var input: SyndFeedInput = _
  var feed: SyndFeed = _

  val titleState: ListBuffer[String] = ListBuffer.empty[String]
  val releaseCounter = new LongCounter()

  var isRunning = false
  var stateClearer: ScheduledFuture[_] = _

  override def open(parameters: Configuration): Unit = {
    input = new SyndFeedInput()
    input.setPreserveWireFeed(true)
    input.setAllowDoctypes(true)

    feed = input.build(new XmlReader(feedUrl))
    isRunning = true

    val ex = new ScheduledThreadPoolExecutor(1)
    val task = new Runnable {
      override def run(): Unit = titleState.clear()
    }
    stateClearer = ex.scheduleAtFixedRate(task, 1, 1, TimeUnit.HOURS)
  }

  override def run(ctx: SourceFunction.SourceContext[PyPiRelease]): Unit = {
    while (isRunning) {
      val state = titleState.toList

      val entries = feed.getEntries.asScala
        .map { x =>
          println(x.getWireEntry)
          val title = x.getTitle
          val link = x.getLink
          val desc = x.getDescription.getValue
          val date = x.getPublishedDate

          PyPiRelease(title, link, desc, date)
        }

      val notEmitted = entries.filter(x => !state.contains(x.title))

      notEmitted.foreach(println)

      /** Emit and add to state */
      releaseCounter.add(notEmitted.size)
      notEmitted.foreach(x => titleState += x.title)
      notEmitted.foreach(x => ctx.collectWithTimestamp(x, x.pubDate.getTime))

      Thread.sleep(waitTime)
    }

    cancel()
  }

  override def cancel(): Unit = {
    isRunning = false
    stateClearer.cancel(true)
  }
}
