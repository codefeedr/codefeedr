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

import org.codefeedr.buffer.serialization._
import org.codefeedr.pipeline._

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._

abstract class TransformStage[In <: PipelineItem : ClassTag: TypeTag : AvroSerde, Out <: PipelineItem : ClassTag: TypeTag : AvroSerde](attributes: StageAttributes = StageAttributes()) extends PipelineObject[In, Out](attributes)
abstract class TransformStage2[In <: PipelineItem : ClassTag: TypeTag : AvroSerde, In2 <: PipelineItem : ClassTag: TypeTag : AvroSerde, Out <: PipelineItem : ClassTag: TypeTag : AvroSerde](attributes: StageAttributes = StageAttributes()) extends PipelineObject2[In, In2, Out](attributes)
abstract class TransformStage3[In <: PipelineItem : ClassTag: TypeTag : AvroSerde, In2 <: PipelineItem : ClassTag: TypeTag : AvroSerde, In3 <: PipelineItem : ClassTag: TypeTag : AvroSerde, Out <: PipelineItem : ClassTag: TypeTag : AvroSerde](attributes: StageAttributes = StageAttributes()) extends PipelineObject3[In, In2, In3, Out](attributes)
abstract class TransformStage4[In <: PipelineItem : ClassTag: TypeTag : AvroSerde, In2 <: PipelineItem : ClassTag: TypeTag : AvroSerde, In3 <: PipelineItem : ClassTag: TypeTag : AvroSerde, In4 <: PipelineItem : ClassTag: TypeTag : AvroSerde, Out <: PipelineItem : ClassTag: TypeTag : AvroSerde](attributes: StageAttributes = StageAttributes()) extends PipelineObject4[In, In2, In3, In4, Out](attributes)