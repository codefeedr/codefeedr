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

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.codefeedr.utilities.Http
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.xml.{Elem, XML}

class RSSItemSource(url: String,
                    pollingInterval: Int = 1000,
                    maxNumberOfRuns: Int = -1,
                    http: Http = new Http) extends RichSourceFunction[RSSItem] {

  var isRunning = false

  override def open(parameters: Configuration): Unit = {
    isRunning = true
  }

  override def cancel(): Unit = {
    isRunning = false
  }

  override def run(ctx: SourceFunction.SourceContext[RSSItem]): Unit = {
    var lastItem: Option[RSSItem] = None
    var numRunsRemaining = maxNumberOfRuns

    while (isRunning && numRunsRemaining != 0) {
      if (numRunsRemaining > 0) {
        numRunsRemaining -= 1
      }

      val nodes = getXMLFromUrl(url) \\ "item"
      val items = for (t <- nodes) yield xmlToRSSItem(t)
      val sortedItems = items.sortWith((x: RSSItem, y: RSSItem) => x.pubDate.isBefore(y.pubDate))
      sortedItems.dropWhile((x: RSSItem) => {
        if (lastItem.isDefined)
          x.pubDate.isBefore(lastItem.get.pubDate) || lastItem.get.guid == x.guid
        else
          false
      })
        .foreach(ctx.collect)

      lastItem = Some(sortedItems.last)

      Thread.sleep(pollingInterval)
    }
  }

  def getXMLFromUrl(url: String) : Elem = {
    XML.loadString(http.getResponse(url).body)
  }

  def xmlToRSSItem(node: scala.xml.Node): RSSItem = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val link = (node \ "link").text
    val guid = (node \ "guid").text

    //Tue, 08 May 2018 08:59:00 GMT
    val formatter = DateTimeFormatter.RFC_1123_DATE_TIME //.ofPattern("EEE, dd MMMMM yyyy HH:mm:ss z")
    val pubDate = LocalDateTime.parse((node \ "pubDate").text, formatter)

    RSSItem(title, description, link, pubDate, guid)
  }


}
