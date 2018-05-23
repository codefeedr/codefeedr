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
 */

package org.codefeedr.plugins.mongodb

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.bson.BsonInt64
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization
import org.mongodb.scala.bson.{BsonDocument, BsonElement, BsonValue}
import org.mongodb.scala.{MongoClient, MongoCollection}
import org.mongodb.scala.bson.collection.mutable.Document

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
  * Mongo sink
  *
  * @param userConfig User configuration. Properties [server, database, collection]
  * @tparam T Type of element that goes into the database
  */
class BaseMongoSink[T <: AnyRef : Manifest : ClassTag](val userConfig: Map[String,String])
  extends RichSinkFunction[T] {

  var client: MongoClient = _

  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all

  override def open(parameters: Configuration): Unit = {
    client = MongoClient(userConfig("server"))
  }

  /**
    * Send a value to mongo
    *
    * Convert the value to JSON and then to a BSON document. Then insert it into the collection.
    *
    * @param value Element
    * @param context Context
    */
  override def invoke(value: T, context: SinkFunction.Context[_]): Unit = {
    val collection = getCollection

    val json = Serialization.write(value)(formats)
    val doc =  Document(json)

    if (context.timestamp() != null) {
      val time: Long = context.timestamp()
      doc += "_eventTime" -> time
    }

    val result = collection.insertOne(doc)

    Await.result(result.toFuture, Duration.Inf)
  }

  /**
    * Get the mongo collection for the configuration.
    *
    * This is not verified to exist; it will be created upon first use.
    *
    * @return Mongo Collection
    */
  def getCollection: MongoCollection[Document] = {
    val database = client.getDatabase(userConfig("database"))

    database.getCollection(userConfig("collection"))
  }

  override def close(): Unit = {
    client.close()
  }
}
