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

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.mongodb.{BaseMongoSource, MongoQuery}
import org.codefeedr.stages.{InputStage, StageAttributes}

import scala.reflect.{ClassTag, Manifest}

/**
  * MongoDB input stage.
  *
  * Reads from the beginning of the collection. If an event time is available in the data, it is set.
  *
  * @param database Name of the database
  * @param collection Name of the collection to read from
  * @param server Optional server address. Format: mongodb://host:port. Defaults to localhost.
  * @param stageAttributes Extra stage attributes
  * @tparam T Type of input
  */
class MongoInput[T <: Serializable with AnyRef : ClassTag : Manifest : TypeInformation](database: String,
                                                                                        collection: String,
                                                                                        server: String = "mongodb://localhost:27017",
                                                                                        query: MongoQuery = MongoQuery.empty,
                                                                                        stageAttributes: StageAttributes = StageAttributes())
  extends InputStage[T](stageAttributes) {

  override def main(): DataStream[T] = {
    val config = Map("database" -> database, "collection" -> collection, "server" -> server)

    val bsonDocument = query.underlyingDocument

    pipeline.environment.addSource(new BaseMongoSource[T](config, bsonDocument))
  }
}
