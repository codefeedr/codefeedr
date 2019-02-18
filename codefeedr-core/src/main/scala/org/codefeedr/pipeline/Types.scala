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
package org.codefeedr.pipeline

/** This class is used for the InputStage input and for the OutputStage output. */
case class NoType()

/** Types for different run-times.
  *
  * Three types of run-modes are available:
  * - Mock: Flink Jobs are linked without buffer, this requires the pipeline to be sequential.
  * - Local: All Flink jobs are run in one [[org.apache.flink.streaming.api.scala.StreamExecutionEnvironment]] connected with buffers.
  * - Cluster: Flink jobs are run separately and connected with buffers.
  */
object RuntimeType extends Enumeration {
  type RuntimeType = Value
  val Mock, Local, Cluster = Value
}

/** Types for different pipelines.
  *
  * Two types of pipelines are available:
  * - Sequential: As the name suggests, only contains stages in linear order.
  * - DAG: Pipeline in the form of a [[DirectedAcyclicGraph]].
  */
object PipelineType extends Enumeration {
  type PipelineType = Value
  val Sequential, DAG = Value
}
