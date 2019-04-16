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

class PyPiReleasesSource(waitTime: Long = 1000)
    extends RichSourceFunction[PyPiRelease] {

  val feedUrl = new URL("https://pypi.org/rss/updates.xml")
  var input: SyndFeedInput = _
  var feed: SyndFeed = _

  val stateConfig = StateTtlConfig
    .newBuilder(Time.hours(12))
    .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
    .setStateVisibility(
      StateTtlConfig.StateVisibility.ReturnExpiredIfNotCleanedUp)
    .cleanupFullSnapshot()
    .build()

  var titleState: ListState[String] = _
  val releaseCounter = new LongCounter()

  var isRunning = false

  override def open(parameters: Configuration): Unit = {
    input = new SyndFeedInput()
    feed = input.build(new XmlReader(feedUrl))

    val titleStateDescriptor =
      new ListStateDescriptor[String]("titles_already_send", classOf[String])
    titleStateDescriptor.enableTimeToLive(stateConfig)

    titleState = getRuntimeContext.getListState(titleStateDescriptor)

    isRunning = true
  }

  override def run(ctx: SourceFunction.SourceContext[PyPiRelease]): Unit = {
    while (isRunning) {
      val state = titleState.get().asScala.toList

      val entries = feed.getEntries.asScala
        .map { x =>
          val title = x.getTitle
          val link = x.getLink
          val desc = x.getDescription.getValue
          val date = x.getPublishedDate

          PyPiRelease(title, link, desc, date)
        }

      val notEmitted = entries.filter(x => !state.contains(x.title))

      /** Emit and add to state */
      releaseCounter.add(notEmitted.size)
      notEmitted.foreach(x => titleState.add(x.title))
      notEmitted.foreach(x => ctx.collectWithTimestamp(x, x.pubDate.getTime))

      Thread.sleep(waitTime)
    }

    cancel()
  }

  override def cancel(): Unit = isRunning = false
}
