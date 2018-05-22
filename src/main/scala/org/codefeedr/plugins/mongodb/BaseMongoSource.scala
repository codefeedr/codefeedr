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
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.bson.json.{JsonMode, JsonWriterSettings}
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection, Observer}

import scala.reflect.ClassTag

class BaseMongoSource[T <: AnyRef : Manifest : ClassTag](val userConfig: Map[String,String]) extends RichSourceFunction[T] {

  var client: MongoClient = _
  var isWaiting = false

  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all

  override def open(parameters: Configuration): Unit = {
    client = MongoClient() // TODO: config options

    isWaiting = true
  }

  override def run(ctx: SourceFunction.SourceContext[T]): Unit = {
    val jsonSettings = JsonWriterSettings.builder()
      .outputMode(JsonMode.RELAXED)
      .build()

    val result = getCollection.find()

    // Use an observer so not all mongo data needs to be loaded into memory,
    // as would be the case when waiting for a List of results
    val observer = new Observer[Document] {

      override def onNext(result: Document): Unit = {
        val json = result.toJson(jsonSettings)
        val element = Serialization.read[T](json)

        ctx.collect(element)
      }

      override def onError(e: Throwable): Unit = {
        println("Failed execution of mongo query", e)
        isWaiting = false
      }

      override def onComplete(): Unit = {
        isWaiting = false
      }
    }

    result.subscribe(observer)

    // Wait for all records to arrive
    while (isWaiting) {
      Thread.sleep(1000)
    }

  }

  override def cancel(): Unit = {
    isWaiting = false
  }

  override def close(): Unit = {
    client.close()
  }

  def getCollection: MongoCollection[Document] = {
    val database = client.getDatabase(userConfig("database"))

    database.getCollection(userConfig("collection"))
  }
}
