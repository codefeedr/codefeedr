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
package org.codefeedr.stages

import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.buffer.serialization.{AvroSerde}
import org.codefeedr.pipeline.{NoType, PipelineItem, PipelineObject}

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._


/**
  * The InputStage class represents the start of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam Out the output type of the job.
  */
abstract class InputStage[Out <: PipelineItem : ClassTag : TypeTag : AvroSerde](attributes: StageAttributes = StageAttributes()) extends PipelineObject[NoType, Out](attributes) {

  override def transform(source: DataStream[NoType]): DataStream[Out] = {
    main()
  }

  /**
    * Create a new datastream
    *
    * @return Stream
    */
  def main(): DataStream[Out]
}
