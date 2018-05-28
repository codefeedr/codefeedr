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
package org.codefeedr.plugins.github


import java.net.URI
import java.time.LocalDateTime
import java.util.Date

import org.apache.avro.Schema
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.buffer.{BufferType, KafkaBuffer}
import org.codefeedr.pipeline.{NoType, PipelineBuilder, PipelineItem}
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._
import org.codefeedr.buffer.serialization.{AvroSerde, Serializer}
import org.codefeedr.plugins.github.GitHubProtocol.Event
import org.codefeedr.stages.utilities.JsonPrinterOutput
import shapeless.datatype.avro.AvroType

case class hoi(lol : Option[Date]) extends PipelineItem

object Main {
  def main(args : Array[String]) : Unit = {

    val serializer = AvroSerde[hoi]
    println(serializer.serialize(hoi(Some(new Date()))))
  }

}

class HoiInput extends InputStage[hoi]{

  override def main(): DataStream[hoi] = pipeline.environment.fromCollection(Seq(hoi(Some(new Date))))
}



