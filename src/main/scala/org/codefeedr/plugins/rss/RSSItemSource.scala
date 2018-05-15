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

import java.lang.Exception

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

    var failedTries = 0

    while (isRunning && numRunsRemaining != 0) {
      try {
        val nodes = getXMLFromUrl(url) \\ "item"

        if (numRunsRemaining > 0) {
          numRunsRemaining -= 1
        }
        if (failedTries > 0) {
          println("Succeeded again. Resetting amount of fails.")
          failedTries = 0
        }


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
      } catch {
        case _: Throwable =>
          failedTries += 1
          println("Failed to get RSS item from url " + failedTries + " time(s)")
          if (failedTries % 3 == 0) {
            val sleepTime = failedTries / 3 * pollingInterval
            println("\t now sleeping for " + sleepTime + " milliseconds")
            Thread.sleep(sleepTime)
          }
      }
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

//    Tue, 08 May 2018 08:59:00 GMT
//    Wed, 09 May 2018 04:03:23 -0400
//    val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
//    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMMMM yyyy HH:mm:ss z")
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMMM yyyy HH:mm:ss Z")
    val pubDate = LocalDateTime.parse((node \ "pubDate").text, formatter)

//    println(title + " " + pubDate.toString)

    RSSItem(title, description, link, pubDate, guid)
  }


}
