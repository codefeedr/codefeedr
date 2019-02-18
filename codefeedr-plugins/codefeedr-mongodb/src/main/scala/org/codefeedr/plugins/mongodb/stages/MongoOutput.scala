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

package org.codefeedr.plugins.mongodb.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.mongodb.BaseMongoSink
import org.codefeedr.stages.{OutputStage, StageAttributes}

import scala.reflect.{ClassTag, Manifest}

/**
  * MongoDB output stage.
  *
  * Writes the data to the collection. If an event time is set, it is also sent to the database.
  *
  * @param database Name of the database
  * @param collection Name of the collection to write to
  * @param server Optional server address. Format: mongodb://host:port. Defaults to localhost.
  * @param stageAttributes Extra stage attributes
  * @tparam T Type of output
  */
class MongoOutput[T <: Serializable with AnyRef: ClassTag: Manifest](
    database: String,
    collection: String,
    server: String = "mongodb://localhost:27017",
    stageAttributes: StageAttributes = StageAttributes())
    extends OutputStage[T](stageAttributes) {

  override def main(source: DataStream[T]): Unit = {
    val config = Map("database" -> database,
                     "collection" -> collection,
                     "server" -> server)

    source.addSink(new BaseMongoSink[T](config))
  }
}
