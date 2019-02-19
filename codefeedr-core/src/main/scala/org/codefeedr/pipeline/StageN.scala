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
package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.DataStream

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** A stage with 2 sources and 1 output.
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam Out Output type for this stage.
  */
abstract class Stage2[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag,
Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage[In, Out](stageId) {

  /** Transforms from type In to type Out.
    *
    * @param source The input source with type In.
    * @return The transformed stream with type Out.
    */
  override def transform(source: DataStream[In]): DataStream[Out] =
    transform(source, getSource[In2](getParents(1)))

  /** Verifies this stage in the graph.
    * Checks if the stage has enough parents and input types are no NoTypes.
    *
    * @param graph The graph the stage is in.
    */
  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    if (typeOf[In] == typeOf[NoType] || typeOf[In2] == typeOf[NoType]) {
      throw new IllegalStateException(
        "Cannot use NoType on stages with multiple input sources")
    }

    if (graph.getParents(this).size < 2) {
      throw new IllegalStateException(
        s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  /** Transform two input streams into a single output stream.
    *
    * @param source The (main) source.
    * @param secondSource The second source.
    * @return An output stream.
    */
  def transform(source: DataStream[In],
                secondSource: DataStream[In2]): DataStream[Out]

}

/** A stage with 3 sources and 1 output
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam In3 Third input type of this stage.
  * @tparam Out Output type for this stage.
  */
abstract class Stage3[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag,
In3 <: Serializable with AnyRef: ClassTag: TypeTag,
Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage2[In, In2, Out](stageId) {

  /** Transforms from type In and In2 to type Out.
    *
    * @param source The input source with type In.
    * @param secondSource The input source with type In2.
    * @return The transformed stream with type Out.
    */
  override def transform(source: DataStream[In],
                         secondSource: DataStream[In2]): DataStream[Out] =
    transform(source, secondSource, getSource[In3](getParents(2)))

  /** Verifies this stage in the graph.
    * Checks if the stage has enough parents and input types are no NoTypes.
    *
    * @param graph The graph the stage is in.
    */
  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    super.verifyGraph(graph)

    if (typeOf[In3] == typeOf[NoType]) {
      throw new IllegalStateException(
        "Cannot use NoType on stages with multiple input sources")
    }

    if (graph.getParents(this).size < 3) {
      throw new IllegalStateException(
        s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  /** Transform three input streams into a single output stream.
    *
    * @param source The (main) source.
    * @param secondSource The second source.
    * @param thirdSource The third source.
    * @return An output stream.
    */
  def transform(source: DataStream[In],
                secondSource: DataStream[In2],
                thirdSource: DataStream[In3]): DataStream[Out]

}

/** A stage with 4 sources and 1 output.
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam In3 Third input type of this stage.
  * @tparam In4 Fourth input type of this stage.
  * @tparam Out Output type for this stage.
  */
abstract class Stage4[In <: Serializable with AnyRef: ClassTag: TypeTag,
In2 <: Serializable with AnyRef: ClassTag: TypeTag,
In3 <: Serializable with AnyRef: ClassTag: TypeTag,
In4 <: Serializable with AnyRef: ClassTag: TypeTag,
Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage3[In, In2, In3, Out](stageId) {

  /** Transforms from type In, In2, In3 to type Out.
    *
    * @param source The input source with type In.
    * @param secondSource The input source with type In2.
    * @param thirdSource The input source with type In3.
    * @return The transformed stream with type Out.
    */
  override def transform(source: DataStream[In],
                         secondSource: DataStream[In2],
                         thirdSource: DataStream[In3]): DataStream[Out] =
    transform(source, secondSource, thirdSource, getSource[In4](getParents(3)))

  /** Verifies this stage in the graph.
    * Checks if the stage has enough parents and input types are no NoTypes.
    *
    * @param graph The graph the stage is in.
    */
  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    super.verifyGraph(graph)

    if (typeOf[In4] == typeOf[NoType]) {
      throw new IllegalStateException(
        "Cannot use NoType on stages with multiple input sources")
    }

    if (graph.getParents(this).size < 4) {
      throw new IllegalStateException(
        s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  /** Transform four input streams into a single output stream.
    *
    * @param source The (main) source.
    * @param secondSource The second source.
    * @param thirdSource The third source.
    * @param fourthSource The fourth source.
    * @return An output stream.
    */
  def transform(source: DataStream[In],
                secondSource: DataStream[In2],
                thirdSource: DataStream[In3],
                fourthSource: DataStream[In4]): DataStream[Out]

}
