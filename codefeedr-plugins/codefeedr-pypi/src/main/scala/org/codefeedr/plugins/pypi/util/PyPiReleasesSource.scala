package org.codefeedr.plugins.pypi.util

import java.net.URL

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{
  RichSourceFunction,
  SourceFunction
}
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiRelease

class PyPiReleasesSource(waitTime: Long = 1000)
    extends RichSourceFunction[PyPiRelease] {

  val feedUrl = new URL("https://pypi.org/rss/updates.xml")

  var input: SyndFeedInput = _
  var feed: SyndFeed = _
  var titleState: ListState[String] = _
  var isRunning = false

  override def open(parameters: Configuration): Unit = {
    input = new SyndFeedInput()
    feed = input.build(new XmlReader(feedUrl))

    val titleStateDescriptor =
      new ListStateDescriptor[String]("titles_already_send", classOf[String])

    titleState = getRuntimeContext.getListState(titleStateDescriptor)

    isRunning = true

  }

  override def run(ctx: SourceFunction.SourceContext[PyPiRelease]): Unit = {
    while (isRunning) {

      Thread.sleep(waitTime)
    }

    cancel()
  }

  override def cancel(): Unit = isRunning = false
}
