package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.plugins.ghtorrent.protocol.GitHub.Event
import org.codefeedr.stages.TransformStage
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.apache.flink.api.scala._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class GHRecordToEventStage[
    T <: Serializable with AnyRef: TypeTag: ClassTag: Event](stageName: String,
                                                             routingKey: String)
    extends TransformStage[Record, T] {

  override def transform(source: DataStream[Record]): DataStream[T] = {
    source
      .filter(_.routingKey == routingKey)
      .map { x =>
        implicit val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all
        parse(x.contents).extract[T]
      }
  }
}
