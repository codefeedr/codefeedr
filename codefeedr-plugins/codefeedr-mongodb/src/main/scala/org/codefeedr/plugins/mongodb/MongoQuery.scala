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

import java.util.Date

import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.gt

/**
  * Mongo query wrapper
  *
  * @param underlying BSON query
  */
case class MongoQuery(underlying: Bson) {

  /**
    * Get a bson document
    * @return
    */
  def underlyingDocument: BsonDocument = underlying match {
    case null => null
    case a: Bson => a.toBsonDocument(BsonDocument.getClass, MongoClient.DEFAULT_CODEC_REGISTRY)
  }

}

object MongoQuery {

  /**
    * A query that gives only items starting at given time
    *
    * @param time Time to start query at
    * @return Mongo Query wrapper
    */
  def from(time: Date): MongoQuery = {
    val bson = gt("_eventTime", time.getTime / 1000)

    new MongoQuery(bson)
  }

  /**
    * Empty query
    * @return Query
    */
  def empty: MongoQuery = new MongoQuery(null)
}
