package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.api.common.functions.{FilterFunction, MapFunction}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.{Event, Record}
import org.codefeedr.stages.TransformStage
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.apache.flink.api.scala._
import scala.reflect.{ClassTag, classTag}
import scala.reflect.runtime.universe._

/** Transforms a GHTRecord to an [[Event]].
  *
  * @param stageName the name of this stage (must be unique per stage).
  * @param routingKey the routing_key to filter on.
  * @tparam T the type of this event.
  */
class GHTRecordToEventStage[
    T <: Serializable with AnyRef with Event: TypeTag: ClassTag: TypeInformation](
    stageName: String,
    var routingKey: String)
    extends TransformStage[Record, T] {

  /** Transforms and parses [[Event]] from [[Record]].
    *
    * @param source The input source with type Record.
    * @return The transformed stream with type T.
    */
  override def transform(source: DataStream[Record]): DataStream[T] = {
    source
      .filter(new RoutingFilter(routingKey))
      .map(new EventExtract[T])
  }

}

/** Filters based on a routing key. */
class RoutingFilter(routingKey: String) extends FilterFunction[Record] {
  override def filter(value: Record): Boolean = value.routingKey == routingKey
}

/** Extracts an event based on the type */
class EventExtract[T: Manifest] extends MapFunction[Record, T] {
  override def map(value: Record): T = {
    implicit val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all
    parse(value.contents).extract[T]
  }
}
