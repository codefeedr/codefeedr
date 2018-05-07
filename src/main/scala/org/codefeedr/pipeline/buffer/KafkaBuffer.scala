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
}

class KafkaBuffer[T <: AnyRef : Manifest : FromRecord](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {

  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  override def getSource: DataStream[T] = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", pipeline.bufferProperties.get(KafkaBuffer.HOST))

    //get correct serde
    val serde = Serializer.getSerde[T](pipeline.bufferProperties.get(KafkaBuffer.SERIALIZER))

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, serde, properties))
  }

  override def getSink: SinkFunction[T] = {
    //get correct serde
    val serde = Serializer.getSerde[T](pipeline.bufferProperties.get(KafkaBuffer.SERIALIZER))

    new FlinkKafkaProducer011[T](pipeline.bufferProperties.get(KafkaBuffer.HOST), topic, serde)
  }
}