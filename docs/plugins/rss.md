## RSS
The RSS source has the following constructor:
```scala
class RSSSource(url: String, pollingInterval: Int) extends Source[RSSItem] {...}
```

It streams the RSS feed as RSSItems. These look like this:

```scala
case class RSSItem(title: String,
                   category: String,
                   link: String
                   pubDate: LocalDateTime,
                   guid: String
                  ) extends PipelineItem
```

A simple example job that creates a RSS source which polls every second and prints the result can be created as follows

```scala
object Main{	
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(new RSSSource("example.com", 1000))
      .append(new MyRSSJob)
      .build()
      .start()
  }
}

class MyRSSJob extends Job[RSSItem] {
  override def main(source: DataStream[RSSItem]): Unit = {
    source.print()
  }
}
```
