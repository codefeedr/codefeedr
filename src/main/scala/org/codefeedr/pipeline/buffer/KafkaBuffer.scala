/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.pipeline.buffer

import java.util.Properties

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.avro.Schema
import org.apache.avro.reflect.ReflectData
import org.codefeedr.Properties._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.codefeedr.pipeline.buffer.serialization._

import scala.reflect.classTag
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.kafka.clients.admin.{AdminClient, AdminClientConfig, KafkaAdminClient, NewTopic}
import org.codefeedr.pipeline.Pipeline
import org.codefeedr.pipeline.buffer.serialization.schema_exposure.{RedisSchemaExposer, ZookeeperSchemaExposer}

import scala.collection.JavaConverters._
import scala.reflect.Manifest

object KafkaBuffer {
  /**
    * PROPERTIES
    */
  val BROKER = "HOST"
  val ZOOKEEPER = "ZOOKEEPER"
  val SERIALIZER = "SERIALIZER"

  //SCHEMA EXPOSURE
  val SCHEMA_EXPOSURE = "SCHEMA_EXPOSURE"
  val SCHEMA_EXPOSURE_SERVICE = "SCHEMA_EXPOSURE_SERVICE"
  val SCHEMA_EXPOSURE_HOST = "SCHEMA_EXPOSURE_HOST"

  /**
    * DEFAULT VALUES
    */
  val DEFAULT_BROKER = "localhost:9092"
  val DEFAULT_ZOOKEEPER = "localhost:2181"

  //SCHEMA EXPOSURE
  val DEFAULT_SCHEMA_EXPOSURE = false
  val DEFAULT_SCHEMA_EXPOSURE_SERVICE = "redis"
  val DEFAULT_SCHEMA_EXPOSURE_HOST = "redis://localhost:6379"
}

class KafkaBuffer[T <: AnyRef : Manifest : FromRecord](pipeline: Pipeline, topic: String) extends Buffer[T](pipeline) {

  //Get type of the class at run time
  val inputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  //get TypeInformation of generic (case) class
  implicit val typeInfo = TypeInformation.of(inputClassType)

  override def getSource: DataStream[T] = {
    val props = pipeline.bufferProperties

    val serde = Serializer.
      getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).
      getOrElse(Serializer.JSON))
    
    //make sure the topic already exists
    checkAndCreateSubject(topic, props.get[String](KafkaBuffer.BROKER).
      getOrElse(KafkaBuffer.DEFAULT_BROKER))

    pipeline.environment.
      addSource(new FlinkKafkaConsumer011[T](topic, serde, getKafkaProperties()))
  }

  override def getSink: SinkFunction[T] = {
    val props = pipeline.bufferProperties

    val serde = Serializer.getSerde[T](props.get[String](KafkaBuffer.SERIALIZER).
      getOrElse(Serializer.JSON))

    //check if a schema should be exposed
    if (props.get[Boolean](KafkaBuffer.SCHEMA_EXPOSURE)
      .getOrElse(KafkaBuffer.DEFAULT_SCHEMA_EXPOSURE)) {
      exposeSchema()
    }

    new FlinkKafkaProducer011[T](topic, serde, getKafkaProperties())
  }

  /**
    * Get all the kafka properties.
    * @return a map with all the properties.
    */
  def getKafkaProperties() : java.util.Properties = {
    val props = pipeline.bufferProperties

    val kafkaProp = new java.util.Properties()
    kafkaProp.put("bootstrap.servers", props.get[String](KafkaBuffer.BROKER).
      getOrElse(KafkaBuffer.DEFAULT_BROKER))
    kafkaProp.put("zookeeper.connect", props.get[String](KafkaBuffer.ZOOKEEPER).
      getOrElse(KafkaBuffer.DEFAULT_ZOOKEEPER))
    kafkaProp.put("auto.offset.reset", "earliest")
    kafkaProp.put("auto.commit.interval.ms", "100")
    kafkaProp.put("enable.auto.commit", "true")

    kafkaProp
  }

  /**
    * Checks if a Kafka topic exists, if not it is created.
    * @param topic the topic to create.
    * @param connection the kafka broker to connect to.
    */
  def checkAndCreateSubject(topic : String, connection : String) = {
    //set all the correct properties
    val props = new Properties()
    props.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, connection)

    //connect with Kafka
    val adminClient = AdminClient.create(props)

    //check if topic already exists
    val alreadyCreated = adminClient
      .listTopics()
      .names()
      .get()
      .contains(topic)

    //if topic doesnt exist yet, create it
    if (!alreadyCreated) {
      //the topic configuration will probably be overwritten by the producer
      //TODO check this ^
      println(s"Topic $topic doesn't exist yet, now creating it.")
      val newTopic = new NewTopic(topic, 1, 1)
      adminClient.
        createTopics(List(newTopic).asJavaCollection)
        .all()
        .get() //this blocks the method until the topic is created
    }
  }

  /**
    * Exposes the Avro schema to an external service (like redis/zookeeper).
    */
  def exposeSchema() = {
    val props = pipeline.bufferProperties

    val exposeName = props.
      get[String](KafkaBuffer.SCHEMA_EXPOSURE_SERVICE).
      getOrElse(KafkaBuffer.DEFAULT_SCHEMA_EXPOSURE_SERVICE)

    val exposeHost = props.
      get[String](KafkaBuffer.SCHEMA_EXPOSURE_HOST).
      getOrElse(KafkaBuffer.DEFAULT_SCHEMA_EXPOSURE_HOST)

    //get exposer
    val exposer = exposeName match {
      case "zookeeper" => new ZookeeperSchemaExposer(exposeHost)
      case _ => new RedisSchemaExposer(exposeHost) //default is redis
    }

    //get the schema
    val schema = ReflectData.get().getSchema(inputClassType)

    //expose the schema
    exposer.putSchema(schema, topic)
  }
}