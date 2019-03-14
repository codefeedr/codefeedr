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
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.codefeedr.plugins.ghtorrent.util.GHTorrentRabbitMQSource
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._

class GHTorrentInputStage(username: String,
                          stageName: String = "ghtorrent_input",
                          host: String = "localhost",
                          port: Int = 5672,
                          routingKeysFile: String = "routing_keys.txt")
    extends InputStage[Record](Some(stageName)) {

  override def main(context: Context): DataStream[Record] = {
    context.env
      .addSource(
        new GHTorrentRabbitMQSource(username, host, port, routingKeysFile))
      .map { x =>
        val splitEl = x.split("#", 2)
        val routingKey = splitEl(0)
        val record = splitEl(1)

        Record(routingKey, record)
      }
  }
}
