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
import org.codefeedr.pipeline._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** The OutputStage class represents the end of a pipeline.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In the input type of the job.
  */
abstract class OutputStage[In <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage[In, Nothing](stageId) {

  /** Transforms the stage from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * In this case it is already implemented as a helper method. To use `main(source: DataStream[In])` instead.
    *
    * @param source The input source with type In.
    * @return The transformed stream. Since it is an OutputStage it will return null.
    */
  override def transform(source: DataStream[In]): DataStream[Nothing] = {
    main(source)

    null
  }

  /** Processes a DataStream.
    *
    * @param source The input source.
    */
  def main(source: DataStream[In]): Unit
}

/** The OutputStage class represents the end of a pipeline with two inputs.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In The input type of the stage.
  * @tparam In2 The second input type of the stage.
  */
abstract class OutputStage2[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage2[In, In2, Nothing](stageId) {

  /** Transforms the stage from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * In this case it is already implemented as a helper method. To use `main(source: DataStream[In])` instead.
    *
    * @param source The input source with type In.
    * @param secondSource The second input source with type In2.
    * @return The transformed stream. Since it is an OutputStage it will return null.
    */
  override def transform(source: DataStream[In],
                         secondSource: DataStream[In2]): DataStream[Nothing] = {
    main(source, secondSource)

    null
  }

  /** Processes a DataStream.
    *
    * @param source The input source.
    * @param secondSource The second input source.
    */
  def main(source: DataStream[In], secondSource: DataStream[In2]): Unit
}

/** The OutputStage class represents the end of a pipeline with three inputs.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In The input type of the stage.
  * @tparam In2 The second input type of the stage.
  * @tparam In3 The third input type of the stage.
  */
abstract class OutputStage3[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag,
In3 <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage3[In, In2, In3, Nothing](stageId) {

  /** Transforms the stage from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * In this case it is already implemented as a helper method. To use `main(source: DataStream[In])` instead.
    *
    * @param source The input source with type In.
    * @param secondSource The second input source with type In2.
    * @param thirdSource The third input source with type In3.
    * @return The transformed stream. Since it is an OutputStage it will return null.
    */
  override def transform(source: DataStream[In],
                         secondSource: DataStream[In2],
                         thirdSource: DataStream[In3]): DataStream[Nothing] = {
    main(source, secondSource, thirdSource)

    null
  }

  /** Processes a DataStream.
    *
    * @param source The input source.
    * @param secondSource The second input source.
    * @param thirdSource The third input source.
    */
  def main(source: DataStream[In],
           secondSource: DataStream[In2],
           thirdSource: DataStream[In3]): Unit
}

/** The OutputStage class represents the end of a pipeline with four inputs.
  * It has an input type but no specific output type since it will not be connected to the buffer.
  *
  * @tparam In The input type of the stage.
  * @tparam In2 The second input type of the stage.
  * @tparam In3 The third input type of the stage.
  * @tparam In4 The fourth input type of the stage.
  */
abstract class OutputStage4[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag,
In3 <: Serializable with AnyRef: ClassTag: TypeTag,
In4 <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage4[In, In2, In3, In4, Nothing](stageId) {

  /** Transforms the stage from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * In this case it is already implemented as a helper method. To use `main(source: DataStream[In])` instead.
    *
    * @param source The input source with type In.
    * @param secondSource The second input source with type In2.
    * @param thirdSource The third input source with type In3.
    * @param fourthSource The fourth input source with type In4.
    * @return The transformed stream. Since it is an OutputStage it will return null.
    */
  override def transform(source: DataStream[In],
                         secondSource: DataStream[In2],
                         thirdSource: DataStream[In3],
                         fourthSource: DataStream[In4]): DataStream[Nothing] = {
    main(source, secondSource, thirdSource, fourthSource)

    null
  }

  /** Processes a DataStream.
    *
    * @param source The input source.
    * @param secondSource The second input source.
    * @param thirdSource The third input source.
    * @param fourthSource The fourth input source.
    */
  def main(source: DataStream[In],
           secondSource: DataStream[In2],
           thirdSource: DataStream[In3],
           fourthSource: DataStream[In4]): Unit
}
