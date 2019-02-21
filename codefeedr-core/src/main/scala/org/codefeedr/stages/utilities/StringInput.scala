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
package org.codefeedr.stages.utilities

import org.apache.flink.streaming.api.scala.{DataStream, _}
import org.codefeedr.pipeline.Context
import org.codefeedr.stages.InputStage

/** Simple String wrapper case class. */
case class StringType(value: String)

/** Create InputStage based on a String.
  *
  * @param str The string to split.
  */
class StringInput(str: String = "", stageId: Option[String] = None)
    extends InputStage[StringType](stageId) {

  /** Splits a String into elements of [[StringType]].
    *
    * @return A newly created DataStream.
    */
  override def main(context: Context): DataStream[StringType] = {
    val list = str.split("[ \n]")

    context.env
      .fromCollection(list)
      .map(StringType(_))
  }

}
