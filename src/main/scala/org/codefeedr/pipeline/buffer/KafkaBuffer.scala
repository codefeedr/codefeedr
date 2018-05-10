package org.codefeedr.pipeline.buffer

import java.util.Properties

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.buffer.serialization._

import scala.reflect.classTag
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Pipeline

import scala.reflect.Manifest

object KafkaBuffer {
  val HOST = "HOST"
  val SERIALIZER = "SERIALIZER"

  val DEFAULT_BROKER = "localhost:9092"

  val MESSAGE_LIMIT = "MESSAGE_LIMIT"
}

class KafkaBuffer[T <: AnyRef : Manifest : FromRecord](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {

  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  override def getSource: DataStream[T] = {
    val props = pipeline.bufferProperties

    val kafkaProp = new java.util.Properties()
    kafkaProp.put("bootstrap.servers", "localhost:9092")
    kafkaProp.put("zookeeper.connect", "localhost:2181")
    kafkaProp.put("auto.offset.reset", "earliest")
    kafkaProp.put("auto.commit.interval.ms", "100")
    kafkaProp.put("enable.auto.commit", "true")

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).getOrElse(Serializer.JSON))

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, serde, kafkaProp))
  }

  override def getSink: SinkFunction[T] = {
    val props = pipeline.bufferProperties

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).getOrElse(Serializer.JSON))

    val kafkaProp = new java.util.Properties()
    kafkaProp.put("bootstrap.servers", "localhost:9092")
    kafkaProp.put("zookeeper.connect", "localhost:2181")

    new FlinkKafkaProducer011[T](topic, serde, kafkaProp)
  }
}