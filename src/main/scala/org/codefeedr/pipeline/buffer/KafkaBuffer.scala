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
  val HOST = "KAFKA_HOST"
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

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", props.get(KafkaBuffer.HOST).getOrElse[String](KafkaBuffer.DEFAULT_BROKER))

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).getOrElse(Serializer.JSON))

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, serde, properties))
  }

  override def getSink: SinkFunction[T] = {
    val props = pipeline.bufferProperties

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).getOrElse(Serializer.JSON))
    new FlinkKafkaProducer011[T](props.get(KafkaBuffer.HOST).getOrElse[String](KafkaBuffer.DEFAULT_BROKER), topic, serde)
  }
}