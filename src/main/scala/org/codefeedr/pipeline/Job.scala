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
package org.codefeedr.pipeline

import com.sksamuel.avro4s.{FromRecord, SchemaFor}
import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.{ClassTag, Manifest}

/**
  * The Job class represents the end of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In the input type of the job.
  */
abstract class Job[In <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, NoType] {

  override def transform(source: DataStream[In]): DataStream[NoType] = {
    main(source)

    null
  }

  /**
    * Use the given datastream
    *
    * @param source
    */
  def main(source: DataStream[In]): Unit
}
