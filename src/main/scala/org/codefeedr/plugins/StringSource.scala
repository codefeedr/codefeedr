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
package org.codefeedr.plugins

import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.codefeedr.pipeline.{NoType, PipelineItem, PipelineObject}

case class StringType(value: String) extends PipelineItem

class StringSource(str : String = "") extends PipelineObject[NoType, StringType] {

  override def transform(source: DataStream[NoType]): DataStream[StringType] = {
    val list = str.split("[ \n]")

    pipeline.environment
      .fromCollection(list).setParallelism(1)
      .map { str => StringType(str) }.setParallelism(1)
  }

}