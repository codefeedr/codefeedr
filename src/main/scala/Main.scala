
import java.time.{LocalDateTime, ZoneId}

import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.codefeedr.pipeline._
import org.codefeedr.pipeline.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.plugins.log.{ApacheAccessLogItem, ApacheLogFileInputStage}
import org.codefeedr.pipeline.buffer.serialization.Serializer
import org.codefeedr.plugins.Printer
import org.codefeedr.plugins.elasticsearch.ElasticSearchOutputStage

object Main {

  def main(args: Array[String]): Unit = {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .setBufferProperty(KafkaBuffer.SERIALIZER, Serializer.JSON)

      .append(new ApacheLogFileInputStage("/Users/joskuijpers/Development/CodeFeedr/access.log"))
      .append(new DemoJobA())
//      .append(new Printer[MyAnalysisItem]())
      .append(new ElasticSearchOutputStage[MyAnalysisItem]("sessions", Set("es://localhost:9300")))
      .build

    pipeline.environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//    pipeline.environment.enableCheckpointing(5000)
    pipeline.startLocal()
  }
}

case class MyAnalysisItem(start: LocalDateTime,
                          end: LocalDateTime,
                          ip: String,
                          amountOfBytes: Int) extends PipelineItem

class DemoJobA extends TransformStage[ApacheAccessLogItem, MyAnalysisItem] {

  override def transform(source: DataStream[ApacheAccessLogItem]): DataStream[MyAnalysisItem] = {
//    source.getExecutionConfig.
    source
      .assignAscendingTimestamps(_.date.atZone(ZoneId.systemDefault()).toEpochSecond)
      .map { line => MyAnalysisItem(line.date, line.date, line.ip, line.amountOfBytes) }
      .keyBy(_.ip)
      .window(EventTimeSessionWindows.withGap(Time.minutes(30)))
      .allowedLateness(Time.seconds(5))
      .reduce((first, second) => MyAnalysisItem(first.start, second.end, first.ip, first.amountOfBytes + second.amountOfBytes))
  }

}
