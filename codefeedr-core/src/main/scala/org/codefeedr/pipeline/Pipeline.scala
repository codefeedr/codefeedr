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
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{
  DataStream,
  StreamExecutionEnvironment
}
import org.codefeedr.Properties
import org.codefeedr.buffer.BufferType.BufferType
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.RuntimeType.RuntimeType

case class Pipeline(var name: String,
                    bufferType: BufferType,
                    bufferProperties: Properties,
                    graph: DirectedAcyclicGraph,
                    keyManager: KeyManager,
                    objectProperties: Map[String, Properties]) {
  var _environment: StreamExecutionEnvironment = _

  val environment: StreamExecutionEnvironment = {
    if (_environment == null) {
      if (false) {
        val conf = new Configuration()

        conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

        _environment =
          StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
      } else {
        _environment = StreamExecutionEnvironment.getExecutionEnvironment
      }

      _environment.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    }

    _environment
  }

  /**
    * Get the properties of a stage
    *
    * @param obj Stage
    * @return Properties
    */
  def propertiesOf[U <: Serializable with AnyRef,
                   V <: Serializable with AnyRef](
      obj: Stage[U, V]): Properties = {
    if (obj == null) {
      throw new IllegalArgumentException("Object can't be null")
    }

    objectProperties.getOrElse(obj.id, new Properties())
  }

  /**
    * Start the pipeline with a list of command line arguments
    *
    * @param args Command line arguments
    */
  def start(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    var runtime = RuntimeType.Local

    // If a pipeline object is specified, set to cluster
    val stage = params.get("stage")
    if (stage != null) {
      runtime = RuntimeType.Cluster
    }

    // Override runtime with runtime parameter
    runtime = params.get("runtime") match {
      case "mock"    => RuntimeType.Mock
      case "local"   => RuntimeType.Local
      case "cluster" => RuntimeType.Cluster
      case _         => runtime
    }

    // Set name if specified
    name = params.get("name", name)

    if (params.has("list") || runtime == RuntimeType.Cluster) {
      validateUniqueness()
    }

    if (params.has("list")) {
      showList(params.has("asException"))
    } else {
      start(runtime, stage, params.get("groupId"))
    }
  }

  private def getNodes
    : Vector[Stage[Serializable with AnyRef, Serializable with AnyRef]] =
    graph.nodes.asInstanceOf[Vector[
      Stage[Serializable with AnyRef, Serializable with AnyRef]]]

  /**
    * Validates the uniqueness of the stage IDs, needed for clustered running
    */
  def validateUniqueness(): Unit = {
    val list = getNodes.map(_.id)
    val uniqList = list.distinct
    val overlap = list.diff(uniqList)

    if (overlap.nonEmpty) {
      throw StageIdsNotUniqueException(overlap.head)
    }
  }

  /**
    * Shows a list of stages inside the pipeline. Option to throw an exception to get the data through Flink.
    * @param asException
    */
  def showList(asException: Boolean): Unit = {
    if (asException) {
      val contents = getNodes
        .map { item =>
          '"' + item.id + '"'
        }
        .mkString(",")
      val json = s"[$contents]"

      throw PipelineListException(json)
    } else {
      getNodes.foreach(item => println(item.id))
    }
  }

  /**
    * Start the pipeline with a run configuration
    *
    * @param runtime Runtime type
    * @param stage   Stage of a cluster run
    */
  def start(runtime: RuntimeType,
            stage: String = null,
            groupId: String = null): Unit = {
    runtime match {
      case RuntimeType.Mock    => startMock()
      case RuntimeType.Local   => startLocal()
      case RuntimeType.Cluster => startClustered(stage, groupId)
    }
  }

  /**
    * Run the pipeline as mock. Only works for sequential pipelines.
    *
    * In a mock run, all stages are put together without buffers and run as a single Flink job.
    */
  def startMock(): Unit = {
    if (!graph.isSequential) {
      throw new IllegalStateException(
        "Mock runtime can't run non-sequential pipelines")
    }

    val objects = getNodes

    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // Connect each object by getting a starting buffer, if any, and sending it to the next.
    var buffer: DataStream[Serializable with AnyRef] = null
    for (obj <- objects) {
      buffer = obj.transform(buffer)
    }

    environment.execute(s"$name: mock")
  }

  /**
    * Start a locally run pipeline.
    *
    * Starts every stage in the same Flink environment but with buffers.
    */
  def startLocal(): Unit = {
    val objects = getNodes

    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // For each PO, make buffers and run
    for (obj <- objects) {
      runObject(obj)
    }

    environment.execute(s"$name: local")
  }

  /**
    * Run the pipeline in a clustered manner: run a single stage only.
    *
    * @param stage Stage to run
    */
  def startClustered(stage: String, groupId: String = null): Unit = {
    val optObj = graph.nodes.find { node =>
      node
        .asInstanceOf[Stage[Serializable with AnyRef, Serializable with AnyRef]]
        .id == stage
    }

    if (optObj.isEmpty) {
      throw StageNotFoundException()
    }

    val obj = optObj.get
      .asInstanceOf[Stage[Serializable with AnyRef, Serializable with AnyRef]]

    obj.setUp(this)
    runObject(obj, groupId)

    environment.execute(s"$name: ${stage}")
  }

  /**
    * Run a pipeline object.
    *
    * Creates a source and sink for the object and then runs the transform function.
    *
    * @param obj
    */
  private def runObject(
      obj: Stage[Serializable with AnyRef, Serializable with AnyRef],
      groupId: String = null): Unit = {
    lazy val source =
      if (obj.hasMainSource) obj.getMainSource(groupId) else null
    lazy val sink = if (obj.hasSink) obj.getSink(groupId) else null

    val transformed = obj.transform(source)

    if (sink != null) {
      transformed.addSink(sink)
    }
  }

}
