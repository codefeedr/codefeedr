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

abstract class TransformStage[In <: Serializable with AnyRef: ClassTag: TypeTag,
Out <: Serializable with AnyRef: ClassTag: TypeTag](
    attributes: StageAttributes = StageAttributes())
    extends PipelineObject[In, Out](attributes)
abstract class TransformStage2[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    attributes: StageAttributes = StageAttributes())
    extends PipelineObject2[In, In2, Out](attributes)
abstract class TransformStage3[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    In3 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    attributes: StageAttributes = StageAttributes())
    extends PipelineObject3[In, In2, In3, Out](attributes)
abstract class TransformStage4[
    In <: Serializable with AnyRef: ClassTag: TypeTag,
    In2 <: Serializable with AnyRef: ClassTag: TypeTag,
    In3 <: Serializable with AnyRef: ClassTag: TypeTag,
    In4 <: Serializable with AnyRef: ClassTag: TypeTag,
    Out <: Serializable with AnyRef: ClassTag: TypeTag](
    attributes: StageAttributes = StageAttributes())
    extends PipelineObject4[In, In2, In3, In4, Out](attributes)
