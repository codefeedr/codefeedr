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