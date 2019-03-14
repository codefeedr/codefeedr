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
package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.plugins.ghtorrent.protocol.GitHub.Commit
import org.codefeedr.stages.TransformStage
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.apache.flink.api.scala._

class GHTRecordToCommitStage extends TransformStage[Record, Commit] {

  private val commitRoutingKey = "ent.commits.insert"

  override def transform(source: DataStream[Record]): DataStream[Commit] = {
    source.filter(_.routingKey == commitRoutingKey).map { x =>
      implicit val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all

      parse(x.contents).extract[Commit]
    }
  }
}
