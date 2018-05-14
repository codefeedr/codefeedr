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

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.DirectedAcyclicGraph

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._


abstract class PipelineObject2[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, Out] {

  override def transform(source: DataStream[In]): DataStream[Out] =
    transform(source, getSource[In2](getParents(1)))

  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    if (typeOf[In] == typeOf[NoType] || typeOf[In2] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (graph.getParents(this).size < 2) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2]): DataStream[Out]

}

abstract class PipelineObject3[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, In3 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject2[In, In2, Out] {

  override def transform(source: DataStream[In], secondSource: DataStream[In2]): DataStream[Out] =
    transform(source, secondSource, getSource[In3](getParents(2)))

  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    super.verifyGraph(graph)

    if (typeOf[In3] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (graph.getParents(this).size < 3) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2], thirdSource: DataStream[In3]): DataStream[Out]

}

abstract class PipelineObject4[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, In3 <: PipelineItem : ClassTag : Manifest : FromRecord, In4 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject3[In, In2, In3, Out] {

  override def transform(source: DataStream[In], secondSource: DataStream[In2], thirdSource: DataStream[In3]): DataStream[Out] =
    transform(source, secondSource, thirdSource, getSource[In4](getParents(3)))

  override def verifyGraph(graph: DirectedAcyclicGraph): Unit = {
    super.verifyGraph(graph)

    if (typeOf[In4] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (graph.getParents(this).size < 4) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2], thirdSource: DataStream[In3], fourthSource: DataStream[In4]): DataStream[Out]

}
