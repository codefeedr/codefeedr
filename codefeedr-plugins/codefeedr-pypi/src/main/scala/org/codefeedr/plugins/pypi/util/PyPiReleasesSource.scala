package org.codefeedr.plugins.pypi.util

import java.net.URL
import java.text.SimpleDateFormat

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

import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import scalaj.http.Http

import scala.xml.XML

class PyPiReleasesSource(pollingInterval: Int = 1000, maxNumberOfRuns: Int = -1)
    extends RichSourceFunction[PyPiRelease] {

  val dateFormat = "EEE, d MMM YYYY HH:mm:ss z"
  val url = "https://pypi.org/rss/updates.xml"
  private var isRunning = false
  private var runsLeft = 0
  private var lastItem: Option[PyPiRelease] = None

  def getIsRunning: Boolean = isRunning

  override def open(parameters: Configuration): Unit = {
    isRunning = true
    runsLeft = maxNumberOfRuns
  }

  override def cancel(): Unit = {
    isRunning = false

  }

  override def run(ctx: SourceFunction.SourceContext[PyPiRelease]): Unit = {
    while (isRunning && runsLeft != 0) {
      try {
        // Polls the RSS feed
        val rssAsString = getRSSAsString
        // Parses the received rss items
        val items: Seq[PyPiRelease] = parseRSSString(rssAsString)

        decreaseRunsLeft()

        // Collect right items and update last item
        val validSortedItems = sortAndDropDuplicates(items)
        validSortedItems.foreach(ctx.collect)
        if (validSortedItems.nonEmpty) {
          lastItem = Some(validSortedItems.last)
        }

        // Wait until the next poll
        waitPollingInterval()
      } catch {
        case _: Throwable =>
      }
    }
  }

  /**
    * Drops items that already have been collected and sorts them based on times
    * @param items Potential items to be collected
    * @return Valid sorted items
    */
  def sortAndDropDuplicates(items: Seq[PyPiRelease]): Seq[PyPiRelease] = {
    items
      .filter((x: PyPiRelease) => {
        if (lastItem.isDefined)
          lastItem.get.pubDate.before(x.pubDate) && lastItem.get.link != x.link
        else
          true
      })
      .sortWith((x: PyPiRelease, y: PyPiRelease) => x.pubDate.before(y.pubDate))
  }

  /**
    * Requests the RSS feed and returns its body as a string.
    * Will keep trying with increasing intervals if it doesn't succeed
    * @return Body of requested RSS feed
    */
  @throws[RequestException]
  def getRSSAsString: String = {
    new HttpRequester().retrieveResponse(Http(url)).body
  }

  /**
    * Parses a string that contains xml with RSS items
    * @param rssString XML string with RSS items
    * @return Sequence of RSS items
    */
  def parseRSSString(rssString: String): Seq[PyPiRelease] = {
    try {
      val xml = XML.loadString(rssString)
      val nodes = xml \\ "item"
      for (t <- nodes) yield xmlToPyPiRelease(t)
    } catch {
      // If the string cannot be parsed return an empty list
      case _: Throwable => Nil
    }
  }

  /**
    * Parses a xml node to a RSS item
    * @param node XML node
    * @return RSS item
    */
  def xmlToPyPiRelease(node: scala.xml.Node): PyPiRelease = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val link = (node \ "link").text

    val formatter = new SimpleDateFormat(dateFormat)
    val pubDate = formatter.parse((node \ "pubDate").text)

    PyPiRelease(title, description, link, pubDate)
  }

  /**
    * If there is a limit to the amount of runs decrease by 1
    */
  def decreaseRunsLeft(): Unit = {
    if (runsLeft > 0) {
      runsLeft -= 1
    }
  }

  /**
    * Wait a certain amount of times the polling interval
    * @param times Times the polling interval should be waited
    */
  def waitPollingInterval(times: Int = 1): Unit = {
    Thread.sleep(times * pollingInterval)
  }

}
