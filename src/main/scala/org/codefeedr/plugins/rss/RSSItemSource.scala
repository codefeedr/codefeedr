package org.codefeedr.plugins.rss

import java.text.SimpleDateFormat

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import scalaj.http.Http

import scala.xml.XML

class RSSItemSource(url: String, pollingInterval: Int) extends RichSourceFunction[RSSItem] {

  var isRunning = false

  override def open(parameters: Configuration): Unit = {
    isRunning = true
  }

  override def cancel(): Unit = {
    isRunning = false
  }

  override def run(ctx: SourceFunction.SourceContext[RSSItem]): Unit = {

    var lastItem = None: Option[RSSItem]

    while (isRunning) {

      val xmlString = Http(url).asString.body
      val xml = XML.loadString(xmlString)

      val nodes = xml \\ "item"
      val items = for (t <- nodes) yield xmlToRSSItem(t)

      val sortedItems = items.sortWith((x: RSSItem, y: RSSItem) => x.pubDate.before(y.pubDate))

      sortedItems.dropWhile((x: RSSItem) => if (lastItem.isDefined)
        x.pubDate.before(lastItem.get.pubDate) || lastItem.get.guid == x.guid
      else false)
        .foreach(ctx.collect)

      lastItem = Some(sortedItems.last)

      Thread.sleep(pollingInterval)
    }
  }

  def xmlToRSSItem(node: scala.xml.Node): RSSItem = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val link = (node \ "link").text
    val guid = (node \ "guid").text

    val format = new SimpleDateFormat("EEE, dd MMMMM yyyy HH:mm:ss z")

    val pubDate = format.parse((node \ "pubDate").text)

    //07 May 2018 12:38:27 GMT

    RSSItem(title, description, link, pubDate, guid)
  }


}
