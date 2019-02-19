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
package org.codefeedr.stages

import org.codefeedr.pipeline._

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** This class represents a TransformStage within a pipeline.
  *
  * @tparam In  Input type for this stage.
  * @tparam Out Output type for this stage.
  */
abstract class TransformStage[In <: Serializable with AnyRef: ClassTag: TypeTag,
Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage[In, Out](stageId)

/** This class represents a TransformStage within a pipeline with two inputs.
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam Out Output type for this stage.
  */
abstract class TransformStage2[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage2[In, In2, Out](stageId)

/** This class represents a TransformStage within a pipeline with three inputs.
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam In3 Third input type for this stage.
  * @tparam Out Output type for this stage.
  */
abstract class TransformStage3[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    In3 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage3[In, In2, In3, Out](stageId)

/** This class represents a TransformStage within a pipeline with four inputs.
  *
  * @tparam In  Input type for this stage.
  * @tparam In2 Second input type for this stage.
  * @tparam In3 Third input type for this stage.
  * @tparam In4 Fourth input type for this stage.
  * @tparam Out Output type for this stage.
  */
abstract class TransformStage4[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    In3 <: Serializable with AnyRef: ClassTag: TypeTag,
    In4 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: Option[String] = None)
    extends Stage4[In, In2, In3, In4, Out](stageId)
