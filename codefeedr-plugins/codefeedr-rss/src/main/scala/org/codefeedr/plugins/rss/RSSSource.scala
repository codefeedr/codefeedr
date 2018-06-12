/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.plugins.rss

import java.text.SimpleDateFormat

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import java.time.format.DateTimeFormatter

import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.utilities.{HttpRequester, RequestException}
import scalaj.http.Http

import scala.xml.XML

class RSSSource(url: String,
                dateFormat: String,
                pollingInterval: Int = 1000,
                maxNumberOfRuns: Int = -1)
  extends RichSourceFunction[RSSItem] with Logging {

  private var isRunning = false
  private var runsLeft = 0
  private var lastItem: Option[RSSItem] = None

  def getIsRunning: Boolean = isRunning

  override def open(parameters: Configuration): Unit = {
    isRunning = true
    runsLeft = maxNumberOfRuns
  }

  override def cancel(): Unit = {
    isRunning = false

  }

  override def run(ctx: SourceFunction.SourceContext[RSSItem]): Unit = {
    while (isRunning && runsLeft != 0) {
      try {
        // Polls the RSS feed
        val rssAsString = getRSSAsString
        // Parses the received rss items
        val items: Seq[RSSItem] = parseRSSString(rssAsString)

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
  def sortAndDropDuplicates(items: Seq[RSSItem]): Seq[RSSItem] = {
    items
      .filter((x: RSSItem) => {
        if (lastItem.isDefined)
          lastItem.get.pubDate.before(x.pubDate) && lastItem.get.guid != x.guid
        else
          true
      }).sortWith((x: RSSItem, y: RSSItem) => x.pubDate.before(y.pubDate))
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
  def parseRSSString(rssString: String): Seq[RSSItem] = {
    try {
      val xml = XML.loadString(rssString)
      val nodes = xml \\ "item"
      for (t <- nodes) yield xmlToRSSItem(t)
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
  def xmlToRSSItem(node: scala.xml.Node): RSSItem = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val link = (node \ "link").text
    val guid = (node \ "guid").text

    val formatter = new SimpleDateFormat(dateFormat)
    val pubDate = formatter.parse((node \ "pubDate").text)

    RSSItem(title, description, link, pubDate, guid)
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
