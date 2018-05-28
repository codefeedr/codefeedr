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

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.{ResultTypeQueryable, TypeExtractor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}
import org.bson.conversions.Bson
import org.bson.json.{JsonMode, JsonWriterSettings}
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.collection.mutable.Document
import org.mongodb.scala.{MongoClient, MongoCollection, Observer}

import scala.reflect.{ClassTag, classTag}

/**
  * Mongo source
  *
  * @param userConfig User configuration. Properties [server, database, collection]
  * @tparam T Type of element that comes from the database
  */
class BaseMongoSource[T <: AnyRef : Manifest : ClassTag](val userConfig: Map[String,String],
                                                         val query: BsonDocument)
  extends RichSourceFunction[T] with ResultTypeQueryable[T] {

  var client: MongoClient = _
  var isWaiting = false

  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all
  val outputClassType: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

  override def open(parameters: Configuration): Unit = {
    client = MongoClient(userConfig("server"))

    isWaiting = true
  }

  /**
    * Runs the mongo source.
    *
    * First gets the collection and executes an empty query on all the data.
    * The data is then received in an async manner sending items to the context.
    */
  override def run(ctx: SourceFunction.SourceContext[T]): Unit = {
    val jsonSettings = JsonWriterSettings.builder()
      .outputMode(JsonMode.RELAXED)
      .build()

    val result = if (query == null) getCollection.find() else getCollection.find(query)

    // Use an observer so not all mongo data needs to be loaded into memory,
    // as would be the case when waiting for a List of results
    val observer = new Observer[Document] {

      override def onNext(result: Document): Unit = {

        val eventTimeVal = result.get("_eventTime")
        if (eventTimeVal.isDefined) {
          val eventTime = eventTimeVal.get.asInt64()

          result -= "_eventTime"

          collect(result, eventTime.longValue())
        } else {
          collect(result)
        }
      }

      def toJson(result: Document): T = {
        val json = result.toJson(jsonSettings)
        Serialization.read[T](json)
      }

      def collect(result: Document): Unit =
        ctx.collect(toJson(result))

      def collect(result: Document, timestamp: Long): Unit =
        ctx.collectWithTimestamp(toJson(result), timestamp)


      override def onError(e: Throwable): Unit = {
        println("Error while reading from mongo:", e)
        isWaiting = false
      }

      override def onComplete(): Unit = {
        isWaiting = false
      }
    }

    result.subscribe(observer)

    // Wait for all records to arrive
    while (isWaiting) {
      Thread.sleep(500)
    }

    cancel()
  }

  override def cancel(): Unit = {
    isWaiting = false
  }

  override def close(): Unit = {
    client.close()
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

  override def getProducedType: TypeInformation[T] =
    TypeExtractor.createTypeInfo(outputClassType)
}
