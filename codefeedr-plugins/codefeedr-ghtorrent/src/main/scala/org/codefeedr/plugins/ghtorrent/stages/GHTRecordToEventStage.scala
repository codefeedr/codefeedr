package org.codefeedr.plugins.ghtorrent.stages

import java.util.Properties

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag}
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.{Event, Record}
import org.codefeedr.stages.TransformStage
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.apache.flink.streaming.connectors.kafka.internal.FlinkKafkaProducer
import org.apache.flink.types.Nothing
import org.apache.flink.util.Collector
import org.codefeedr.buffer.serialization.Serializer
import org.codefeedr.stages.kafka.KafkaOutput
import org.slf4j.Logger

import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.universe._

/** Transforms a GHTRecord to an [[Event]].
  *
  * @param stageName the name of this stage (must be unique per stage).
  * @param routingKey the routing_key to filter on.
  * @tparam T the type of this event.
  */
protected class GHTRecordToEventStage[
    T <: Serializable with AnyRef with Event: TypeTag: ClassTag: TypeInformation](
    stageName: String,
    routingKey: String,
    sideOutput: Boolean = true,
    sideOutputTopic: String = "parse_exception",
    sideOutputKafkaServer: String = "localhost:9092")
    extends TransformStage[Record, T](Some(stageName)) {

  val outputTag = OutputTag[Record]("parse_exception")

  /** Transforms and parses [[Event]] from [[Record]].
    *
    * @param source The input source with type Record.
    * @return The transformed stream with type T.
    */
  override def transform(source: DataStream[Record]): DataStream[T] = {
    val trans = source
      .process(new EventExtract[T](routingKey, outputTag))

    if (sideOutput) {
      trans
        .getSideOutput(outputTag)
        .addSink(
          new FlinkKafkaProducer011[Record](
            sideOutputKafkaServer,
            sideOutputTopic,
            Serializer.getSerde[Record](Serializer.JSON)))
    }

    trans
  }

}

/** Filters on routing key and extracts */
class EventExtract[T: Manifest](routingKey: String,
                                outputTag: OutputTag[Record])
    extends ProcessFunction[Record, T] {

  implicit lazy val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all

  override def processElement(value: Record,
                              ctx: ProcessFunction[Record, T]#Context,
                              out: Collector[T]): Unit = {
    if (value.routingKey != routingKey) return //filter on routing keys

    // Extract it into an optional.
    val parsedEvent = parse(value.contents).extractOpt[T]

    if (parsedEvent.isEmpty) {
      ctx.output(outputTag, value)
    } else {
      out.collect(parsedEvent.get)
    }
  }
}
