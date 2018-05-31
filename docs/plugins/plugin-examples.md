## GitHub
The GitHub plugin has several stages. In the section below each stage will be discussed and lastly some sample pipelines will be discussed. See [GitHubProtocol](https://github.com/joskuijpers/bep_codefeedr/blob/develop/src/main/scala/org/codefeedr/plugins/github/GitHubProtocol.scala) for all the case classes used.

### Stages
#### GitHubEventsInput
Reads events from the '/event' endpoint of GitHub. Per poll often around +- 300 events are included, which means around 3 GitHub requests (100 per request). In the list below the constructor parameters are explained:
- `numOfPolls`: to specify the amount of polls before it stops, `-1` for unbounded (`-1` is default).
- `waitTime`: to specify the amount of milliseconds it should wait before doing a new poll. Make sure choose a wait time based on the amount of keys/request you have available. E.g. if you have `5000` request p/h, you can make around `5000/3 = 1666` polls p/h, `3600/1666= 2.16` seconds between each poll. (`1000` is default).
-  `duplicateFilter`: to enable a duplicate filter. This source runs single-threaded, so the event id's can be cached to check for duplicates. (`false` is default).
- `duplicateCheckSize`: the amount of event id's to cache in case the duplicateFilter is enabled. (`1000000` is default)

**Note**: this stage makes use of the [KeyManager](#key-manager). Make sure to configure it properly with the keys you want to use for this stage. 

#### GitHubEventToPushEvent
This stage reads from the event timeline and filters it into a push-event stream. 

### Sample pipelines
In this section some sample pipelines will be discussed.
 
**Note**: These samples do not show configuration of for instance key management. See the sections above to show the configurable options.
#### Real-time PushEvent stream
```scala
new PipelineBuilder()
  .setBufferType(BufferType.Kafka)
  .append(new GitHubEventsInput())
  .append(new GitHubEventToPushEvent)
  .build()
  .startLocal()
```
This pipeline will create a real-time pushevents stream by reading from a the `/events` endpoint (GitHubEventsInput stage) and filter (& parse) the PushEvents (GitHubEventsToPushEvent stage).

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

## Apache Access Logs

The Apache Log Source has the following construcor:

```scala
class ApacheLogFileSource(absolutePath: String) extends Source[ApacheAccessLogItem] with Serializable {...}
````

It stream the log in ApacheAccessLogItems:
```scala
case class ApacheAccessLogItem(ipAdress: String,
                               date: LocalDateTime,
                               request: String,
                               status: Int,
                               amountOfBytes: Int,
                               referer: String,
                               userAgent: String) extends PipelineItem
```

A simple example job that creates a Apache Log source that prints the result can be created as follows

```scala
object Main{	
  def main(args: Array[String]): Unit = {
    new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(new ApacheLogFileSource("/Path/to/file/example.log"))
      .append(new MyRSSJob)
      .build()
      .start()
  }
}

class MyLogJob extends Job[ApacheAccessLogItem] {
  override def main(source: DataStream[ApacheAccessLogItem]): Unit = {
    source.print()
  }
}
```