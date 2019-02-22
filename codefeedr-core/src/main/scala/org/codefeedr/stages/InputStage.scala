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
import org.codefeedr.pipeline.{Context, Stage}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** The InputStage class represents the start of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam Out The output type of the job.
  */
abstract class InputStage[Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage[Nothing, Out](stageId) {

  /** Transforms the stage from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * In this case it is already implemented as a helper method. To use `main(): DataStream[Out]` instead.
    *
    * @param source The input source with type In. In the case of an InputStage it is a NoType.
    * @return The transformed stream with type Out.
    */
  override def transform(source: DataStream[Nothing]): DataStream[Out] = {
    main(getContext)
  }

  /** Creates a DataStream with type Out.
    *
    * @return A newly created DataStream.
    */
  def main(context: Context): DataStream[Out]

}
