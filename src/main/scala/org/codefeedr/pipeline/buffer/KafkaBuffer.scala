package org.codefeedr.pipeline.buffer
import java.util.Properties

//import org.apache.flink.streaming.api.scala._
//import org.apache.flink.api.common.serialization.{DeserializationSchema, SerializationSchema, SimpleStringSchema}
//import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011.Semantic
//import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.Pipeline

object KafkaBuffer {
  val HOST = "KAFKA_HOST"
}

class KafkaBuffer[T](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {

  override def getSource: DataStream[T] = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", pipeline.bufferProperties.get(KafkaBuffer.HOST))
    //    properties.setProperty("group.id", "test")

    //    pipeline.getEnvironment.addSource(new FlinkKafkaConsumer011[T](topic, getDeserializationSchema(topic), properties))
    pipeline.bufferProperties.get(KafkaBuffer.HOST)
    null
  }

  override def getSink: SinkFunction[T] = {
//    new FlinkKafkaProducer011[T](KafkaBuffer.brokerList, topic, getSerializationSchema(topic))
    null
  }


//  private def getDeserializationSchema(topic: String): DeserializationSchema[T] = {
//
//    new SimpleStringSchema()
//
//  }
//
//  private def getSerializationSchema(topic: String): SerializationSchema[T] = {
//
//    new SimpleStringSchema()
//
//  }
}