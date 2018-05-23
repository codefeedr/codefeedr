package org.codefeedr.plugins.rss

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.codefeedr.utilities.Http
import org.scalamock.function.FunctionAdapter1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSuite}

import scala.io.Source
import scalaj.http.HttpResponse

class RSSSourceTest extends FunSuite with MockFactory with BeforeAndAfter {


  test("RSS source should poll maxNumberOfRuns times") {

    val httpMock = mock[Http]
    val fakeUrl = "http://www.example.com"
    val rssItemSource = new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z", 2000, 2, httpMock)

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    for (line <- lines) {
      val response = HttpResponse[String](line, 0, null)
      (httpMock.getResponse _).expects(*).returning(response).noMoreThanOnce()
    }

    //Needed to tell ScalaMock that collect() will be called (without caring about the arguments and how often)
    (ctxMock.collect _).expects(*).anyNumberOfTimes()

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)
  }

  test("RSS source should collect all RSS items"){
    val httpMock = mock[Http]
    val fakeUrl = "http://www.example.com"
    val rssItemSource = new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",2000, 2, httpMock)

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    for (line <- lines) {
      val response = HttpResponse[String](line, 0, null)
      (httpMock.getResponse _).expects(*).returning(response).noMoreThanOnce()
    }

    //Exactly 15 RSS items should be collected, because thats how many unique ones there are
    (ctxMock.collect _).expects(*).repeated(15)

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)
  }

  test("RSS source should collect RSS items in order"){
    val httpMock = mock[Http]
    val fakeUrl = "http://www.example.com"
    val rssItemSource = new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z", 2000, 2, httpMock)

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    for (line <- lines) {
      val response = HttpResponse[String](line, 0, null)
      (httpMock.getResponse _).expects(*).returning(response).noMoreThanOnce()
    }

    //Add RSS items to a list to check later
    var rssItemList: List[RSSItem] = List()
    (ctxMock.collect _).expects(new FunctionAdapter1[RSSItem, Boolean]((x:RSSItem) => {rssItemList = x :: rssItemList; true})).anyNumberOfTimes()

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)

    //RSS items should already be in order
    val orderedRSSItemList = rssItemList.sortWith((x,y) => y.pubDate.isBefore(x.pubDate))
    assert(rssItemList.equals(orderedRSSItemList))
  }

  test("RSS source should continue when recieving wrong xml"){
    val httpMock = mock[Http]
    val fakeUrl = "http://www.example.com"
    val rssItemSource = new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",2000, 5, httpMock)

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponsesWithFailedXML"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines

    for (line <- lines) {
      val response = HttpResponse[String](line, 0, null)
      (httpMock.getResponse _).expects(*).returning(response).noMoreThanOnce()
    }

    //Exactly 15 RSS items should be collected, because thats how many unique ones there are
    (ctxMock.collect _).expects(*).repeated(15)

    rssItemSource.open(null)
    rssItemSource.run(ctxMock)
  }

  test("RSS source should collect all RSS items even when not receiving http responses"){
    val httpMock = mock[Http]
    val fakeUrl = "http://www.example.com"
    val rssItemSource = new RSSSource(fakeUrl, "EEE, dd MMMM yyyy HH:mm:ss z",2000, 2, httpMock)

    val ctxMock = mock[SourceFunction.SourceContext[RSSItem]]

    val rssResponsesName = "/RSSItemSourceTestResponses"
    val lines = Source.fromURL(getClass.getResource(rssResponsesName)).getLines


    (httpMock.getResponse _).expects(*).throwing(null).repeated(3)

    for (line <- lines) {
      val response = HttpResponse[String](line, 0, null)
      (httpMock.getResponse _).expects(*).returning(response).noMoreThanOnce()
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
