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
 */
package org.codefeedr.plugins.rss

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.logging.log4j.scala.Logging
import org.codefeedr.stages.utilities.RequestException
import org.scalamock.function.FunctionAdapter1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.mockito.Mockito._

import scala.io.Source

class RSSSourceTest extends FunSuite with MockFactory with BeforeAndAfter with Logging {


  test("RSS source should poll maxNumberOfRuns times") {
    logger.info("RSS Test 1 start")
    val fakeUrl = "http://www.example.com"
    val rssItemSource = spy(new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z", 0, 2))

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    var stubbing = when(rssItemSource.getRSSAsString)

    for (line <- lines) {
      stubbing = stubbing.thenReturn(line)
    }

    //Needed to tell ScalaMock that collect() will be called (without caring about the arguments and how often)
    (ctxMock.collect _).expects(*).anyNumberOfTimes()

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)

    logger.info("RSS Test 1 stop")
  }

  test("RSS source should collect all RSS items"){
    logger.info("RSS Test 2 start")
    val fakeUrl = "http://www.example.com"
    val rssItemSource = spy(new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",0, 2))

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    var stubbing = when(rssItemSource.getRSSAsString)

    for (line <- lines) {
      stubbing = stubbing.thenReturn(line)
    }

    //Exactly 15 RSS items should be collected, because thats how many unique ones there are
    (ctxMock.collect _).expects(*).repeated(15)

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)

    logger.info("RSS Test 2 stop")
  }

  test("RSS source should collect RSS items in order"){
    logger.info("RSS Test 3 start")

    val fakeUrl = "http://www.example.com"
    val rssItemSource = spy(new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z", 0, 2))

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    var stubbing = when(rssItemSource.getRSSAsString)

    for (line <- lines) {
      stubbing = stubbing.thenReturn(line)
    }

    //Add RSS items to a list to check later
    var rssItemList: List[RSSItem] = List()
    (ctxMock.collect _).expects(new FunctionAdapter1[RSSItem, Boolean]((x:RSSItem) => {rssItemList = x :: rssItemList; true})).anyNumberOfTimes()

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)

    //RSS items should already be in order
    val orderedRSSItemList = rssItemList.sortWith((x,y) => y.pubDate.before(x.pubDate))
    assert(rssItemList.equals(orderedRSSItemList))
    logger.info("RSS Test 3 stop")
  }

  test("RSS source should continue when recieving wrong xml"){
    logger.info("RSS Test 4 start")
    val fakeUrl = "http://www.example.com"
    val rssItemSource = spy(new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",0, 5))

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponsesWithFailedXML"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    var stubbing = when(rssItemSource.getRSSAsString)

    for (line <- lines) {
      stubbing = stubbing.thenReturn(line)
    }

    //Exactly 15 RSS items should be collected, because thats how many unique ones there are
    (ctxMock.collect _).expects(*).repeated(15)

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)
    logger.info("RSS Test 4 stop")
  }

  test("RSS source should collect all RSS items even when request gives exception"){
    logger.info("RSS Test 5 start")
    val fakeUrl = "http://www.example.com"
    val rssItemSource = spy(new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",0, 2))

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponsesWithEmptyLine"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    var stubbing = when(rssItemSource.getRSSAsString)

    for (line <- lines) {
      if (line.isEmpty) {
        stubbing = stubbing.thenThrow(RequestException())
      } else {
        stubbing = stubbing.thenReturn(line)
      }
    }

    //Exactly 15 RSS items should be collected, because thats how many unique ones there are
    (ctxMock.collect _).expects(*).repeated(15)

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)
    assert(true)
    logger.info("RSS Test 5 stop")
  }

  test("Cancel should turn make isRunning false") {
    logger.info("RSS Test 6 start")
    val source = new RSSSource("", "EEE, dd MMMM yyyy HH:mm:ss z",0)
    assert(!source.getIsRunning)
    source.open(null)
    assert(source.getIsRunning)
    source.cancel()
    assert(!source.getIsRunning)
    logger.info("RSS Test 6 stop")
  }

}
