package org.codefeedr.plugins.rss

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.scalamock.function.FunctionAdapter1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.mockito.Mockito._

import scala.io.Source
import scalaj.http.HttpResponse

class RSSSourceTest extends FunSuite with MockFactory with BeforeAndAfter {


  test("RSS source should poll maxNumberOfRuns times") {
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
  }

  test("RSS source should collect all RSS items"){
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
  }

  test("RSS source should collect RSS items in order"){
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
  }

  test("RSS source should continue when recieving wrong xml"){
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
  }

  test("RSS source should collect all RSS items even when not receiving http responses"){
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
  }

  test("Cancel should turn make isRunning false") {
    val source = new RSSSource("", "EEE, dd MMMM yyyy HH:mm:ss z",0)
    assert(!source.getIsRunning)
    source.open(null)
    assert(source.getIsRunning)
    source.cancel()
    assert(!source.getIsRunning)
  }

}
