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

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.codefeedr.Properties
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.buffer.BufferType.BufferType
import org.codefeedr.pipeline.RuntimeType.RuntimeType

case class Pipeline(bufferType: BufferType,
                    bufferProperties: Properties,
                    graph: DirectedAcyclicGraph,
                    properties: Properties,
                    keyManager: KeyManager) {
  val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  def start(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    var runtime = RuntimeType.Local

    // If a pipeline object is specified, set to cluster
    val stage = params.get("stage")
    if (stage != null) {
      runtime = RuntimeType.Cluster
    }

    // Override runtime with runtime parameter
    val mapping = Map(
      "mock" -> RuntimeType.Mock,
      "local" -> RuntimeType.Local,
      "cluster" -> RuntimeType.Cluster
    )

    runtime = mapping.getOrElse(params.get("runtime"), runtime)

    start(runtime, stage)
  }

  def start(runtime: RuntimeType, stage: String = null): Unit = {
    runtime match {
      case RuntimeType.Mock => startMock()
      case RuntimeType.Local => startLocal()
      case RuntimeType.Cluster => startClustered(stage)
    }
  }

  // Without any buffers. Connect all POs to each other
  def startMock(): Unit = {
    if (!graph.isSequential) {
      throw new IllegalStateException("Mock runtime can't run non-sequential pipelines")
    }

    val objects = graph.nodes.asInstanceOf[Vector[PipelineObject[PipelineItem, PipelineItem]]]

    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // Connect each object by getting a starting buffer, if any, and sending it to the next.
    var buffer: DataStream[PipelineItem] = null
    for (obj <- objects) {
      buffer = obj.transform(buffer)
    }

    environment.execute("CodeFeedr Mock Job")
  }

  // With buffers, all in same program
  def startLocal(): Unit = {
    val objects = graph.nodes.asInstanceOf[Vector[PipelineObject[PipelineItem, PipelineItem]]]

    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // For each PO, make buffers and run
    for (obj <- objects) {
      runObject(obj)
    }

    environment.execute("CodeFeedr Local Job")
  }

  // With buffers, running just one PO
  def startClustered(stage: String): Unit = {
    val optObj = graph.nodes.find(node => node.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]].id == stage)

    if (optObj.isEmpty) {
      throw StageNotFoundException()
    }

    val obj = optObj.get.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]]

    obj.setUp(this)
    runObject(obj)

    environment.execute("CodeFeedr Cluster Job")
  }

  /**
    * Run a pipeline object.
    *
    * Creates a source and sink for the object and then runs the transform function.
    * @param obj
    */
  private def runObject(obj: PipelineObject[PipelineItem, PipelineItem]): Unit = {
    lazy val source = if (obj.hasMainSource) obj.getMainSource else null
    lazy val sink = if (obj.hasSink) obj.getSink else null

    val transformed = obj.transform(source)

    if (sink != null) {
      transformed.addSink(sink)
    }
  }

}