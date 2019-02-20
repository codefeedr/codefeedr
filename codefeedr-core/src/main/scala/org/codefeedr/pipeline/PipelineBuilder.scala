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

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{
  DataStream,
  StreamExecutionEnvironment
}
import org.apache.logging.log4j.scala.Logging
import org.codefeedr.Properties
import org.codefeedr.buffer.{Buffer, BufferType}
import org.codefeedr.buffer.BufferType.BufferType
import org.codefeedr.buffer.serialization.Serializer
import org.codefeedr.buffer.serialization.Serializer.SerializerType
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.PipelineType.PipelineType
import org.codefeedr.stages.{InputStage, OutputStage}

import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** Mutable class to build a [[Pipeline]]. A pipeline represents a set of stages interconnected with a [[org.codefeedr.buffer.Buffer]].
  * This pipeline gets translated into a [[DirectedAcyclicGraph]].
  *
  * This builder allows for setting the following properties:
  * - Name of the pipeline.
  * - Type of buffer (e.g. Kafka) and properties for this buffer.
  * - Type of pipeline: sequential or DAG (nonsequential).
  * - Properties for all the stages.
  * - [[KeyManager]] for all the stages.
  */
class PipelineBuilder extends Logging {

  /** Type of buffer used in the pipeline */
  protected var bufferType: BufferType = BufferType.Kafka

  /** Type of the pipeline graph */
  protected var pipelineType: PipelineType = PipelineType.Sequential

  /** Properties of the buffer */
  protected[pipeline] var bufferProperties = new Properties()

  /** Stage properties */
  protected val stageProperties = new mutable.HashMap[String, Properties]()

  /** Key manager */
  protected var keyManager: KeyManager = _

  /** Pipeline verification toggle, defaults to true **/
  protected var pipelineVerificationToggle: Boolean = true

  /** The StreamTimeCharacteristic. Default: [[TimeCharacteristic.EventTime]]*/
  protected var streamTimeCharacteristic: TimeCharacteristic =
    TimeCharacteristic.EventTime

  /** Graph of the pipeline */
  protected[pipeline] var graph = new DirectedAcyclicGraph()

  /** Last inserted pipeline object, used to convert sequential to dag. */
  private var lastStage: AnyRef = _

  /** The name of the pipeline, "CodeFeedr pipeline" by default. */
  protected var name = "CodeFeedr pipeline"

  /** Get the type of the buffer.
    *
    * @return The buffer type.
    */
  def getBufferType: BufferType = bufferType

  /** Set the type of the buffer.
    *
    * @param bufferType The new buffer type.
    * @return The builder instance.
    */
  def setBufferType(bufferType: BufferType): PipelineBuilder = {
    this.bufferType = bufferType

    this
  }

  /** Get the type of the pipeline.
    *
    * @return Type of pipeline: Sequential or DAG.
    */
  def getPipelineType: PipelineType = pipelineType

  /** Disables the pipeline verification.
    * This is not recommended, it allows for nasty pipelines.
    *
    * @return The builder instance.
    */
  def disablePipelineVerification(): PipelineBuilder = {
    this.pipelineVerificationToggle = false

    this
  }

  /** Set the type of the pipeline.
    *
    * @param pipelineType Type of the pipeline.
    * @return This builder instance.
    */
  def setPipelineType(pipelineType: PipelineType): PipelineBuilder = {
    if (pipelineType == PipelineType.Sequential && this.pipelineType == PipelineType.DAG) {
      if (!graph.isSequential) {
        throw new IllegalStateException(
          "The current non-sequential pipeline can't be turned into a sequential pipeline.")
      }

      lastStage = graph.lastInSequence.get
    }

    this.pipelineType = pipelineType

    this
  }

  /** Set a buffer property.
    * A buffer property is generic for all buffers (like the serializer).
    *
    * @param key Key of the property.
    * @param value Value of the property.
    * @return This builder instance.
    */
  def setBufferProperty(key: String, value: String): PipelineBuilder = {
    bufferProperties = bufferProperties.set(key, value)

    this
  }

  /** Set a stage property.
    *
    * @param id The stage to assign the property to.
    * @param key Key of the property.
    * @param value Value of the property.
    * @return This builder instance.
    */
  def setStageProperty(stage: Stage[_, _],
                       key: String,
                       value: String): PipelineBuilder = {

    val properties = stageProperties.getOrElse(stage.id, new Properties())
    stageProperties.put(stage.id, properties.set(key, value))

    this
  }

  /** Set a [[KeyManager]].
    * A key manager handles API key management and can be accessed by every stage.
    *
    * @param km The key manager instance.
    * @return This builder instance.
    */
  def setKeyManager(km: KeyManager): PipelineBuilder = {
    this.keyManager = km

    this
  }

  /** Set name of the pipeline.
    *
    * @param name Name of the pipeline.
    * @return This builder instance.
    */
  def setPipelineName(name: String): PipelineBuilder = {
    this.name = name

    this
  }

  /** Set TimeCharacteristic of the whole pipeline.
    *
    * @param timeCharacteristic The TimeCharacterisic.
    * @return This builder instance.
    */
  def setStreamTimeCharacteristic(timeCharacteristic: TimeCharacteristic) = {
    this.streamTimeCharacteristic = timeCharacteristic

    this
  }

  /** Sets the serializer type for the buffer.
    *
    * @param serializer The serializer type (which is basically a string).
    * @return This builder instance.
    */
  def setSerializer(serializer: SerializerType) = {
    this.setBufferProperty(Buffer.SERIALIZER, serializer)

    this
  }

  /** Append a [[Stage]] in a sequential pipeline.
    *
    * @param stage The new stage to add.
    * @tparam IN Incoming type of the stage.
    * @tparam OUT Outgoing type of the stage.
    * @return This builder instance.
    */
  def append[IN <: Serializable with AnyRef, OUT <: Serializable with AnyRef](
      stage: Stage[IN, OUT]): PipelineBuilder = {

    // You cannot append to a non-sequential pipeline.
    if (pipelineType != PipelineType.Sequential) {
      throw new IllegalStateException(
        "Can't append node to non-sequential pipeline.")
    }

    if (graph.hasNode(stage)) {
      throw new IllegalArgumentException("Item already in sequence.")
    }

    graph = graph.addNode(stage)

    if (lastStage != null) {
      graph = graph.addEdge(lastStage, stage)
    }

    lastStage = stage

    this
  }

  /** Append a stage created from an input function.
    *
    * @param input Input function.
    * @tparam In Type of the [[DataStream]].
    * @return The builder instance.
    */
  def appendSource[In <: Serializable with AnyRef: ClassTag: TypeTag](
      input: StreamExecutionEnvironment => DataStream[In]): PipelineBuilder = {
    val pipelineItem = new InputStage[In] {
      override def main(context: Context): DataStream[In] = input(context.env)
    }

    append(pipelineItem)
  }

  /** Append a stage created from a transform function.
    *
    * @param trans Transform function from one type to another.
    * @tparam In Incoming type of the [[DataStream]].
    * @tparam Out Outgoing type of the [[DataStream]].
    * @return The builder instance.
    */
  def append[In <: Serializable with AnyRef: ClassTag: TypeTag,
             Out <: Serializable with AnyRef: ClassTag: TypeTag](
      trans: DataStream[In] => DataStream[Out]): PipelineBuilder = {
    val pipelineItem = new Stage[In, Out] {
      override def transform(source: DataStream[In]): DataStream[Out] =
        trans(source)
    }

    append(pipelineItem)
  }

  /** Append a stage created from an output function.
    *
    * @param trans Output function.
    * @tparam In Type of the [[DataStream]].
    * @return The builder instance.
    */
  def append[In <: Serializable with AnyRef: ClassTag: TypeTag](
      trans: DataStream[In] => Any): PipelineBuilder = {
    val pipelineItem = new OutputStage[In] {
      override def main(source: DataStream[In]): Unit = trans(source)
    }

    append(pipelineItem)
  }

  /** Creates an edge between two stages. The 'to' must not already have a parent.
    *
    * If the graph is not configured yet (has no nodes), the graph is switched to a DAG automatically.
    * If it was already configured as sequential, it will throw an [[IllegalStateException]].
    *
    * @param from The stage to start from.
    * @param to The stage to end at.
    * @param checkEdge Check if edge already exists (and throw exception if so).
    * @tparam In Incoming type from 'start' stage.
    * @tparam Mid1 Intermediate type which is output for 'start' stage.
    * @tparam Mid2 Intermediate type which is output for 'end' stage.
    * @tparam Out Outgoing type for 'end' stage.
    */
  def edge[In <: Serializable with AnyRef,
           Mid1 <: Serializable with AnyRef,
           Mid2 <: Serializable with AnyRef,
           Out <: Serializable with AnyRef](
      from: Stage[In, Mid1],
      to: Stage[Mid2, Out]): PipelineBuilder = {
    makeEdge(from, to)

    this
  }

  /** Creates an edge between two stages. The 'to' must not already have a parent.
    *
    * If the graph is not configured yet (has no nodes), the graph is switched to a DAG automatically.
    * If it was already configured as sequential, it will throw an [[IllegalStateException]].
    *
    * @param from The stage to start from.
    * @param to The stage to end at.
    * @param checkEdge Check if edge already exists (and throw exception if so).
    * @tparam In Incoming type from 'start' stage.
    * @tparam Mid1 Intermediate type which is output for 'start' stage.
    * @tparam Mid2 Intermediate type which is output for 'end' stage.
    * @tparam Out Outgoing type for 'end' stage.
    */
  private def makeEdge[In <: Serializable with AnyRef,
                       Mid1 <: Serializable with AnyRef,
                       Mid2 <: Serializable with AnyRef,
                       Out <: Serializable with AnyRef](from: Stage[In, Mid1],
                                                        to: Stage[Mid2, Out],
                                                        checkEdge: Boolean =
                                                          true): Unit = {
    if (pipelineType != PipelineType.DAG) {
      if (!graph.isEmpty) {
        throw new IllegalStateException(
          "Can't append node to non-sequential pipeline.")
      }

      pipelineType = PipelineType.DAG
    }

    // Add stages if they don't exist yet.
    if (!graph.hasNode(from)) graph = graph.addNode(from)
    if (!graph.hasNode(to)) graph = graph.addNode(to)

    // Check if edge already existed (if this needs to be checked).
    if (checkEdge && graph.hasEdge(from, to)) {
      throw new IllegalArgumentException("Edge in graph already exists")
    }

    // Create it.
    graph = graph.addEdge(from, to)
  }

  /** Add multiple parents (stages) in given ordered list.
    *
    * @param stage Stage to add the parents to.
    * @param parents Parents to connect to the child {@code stage}.
    * @tparam In Incoming type of the stage.
    * @tparam Out Outgoing type of the stage.
    * @return The builder instance.
    */
  def addParents[In <: Serializable with AnyRef,
                 Out <: Serializable with AnyRef](
      stage: Stage[In, Out],
      parents: StageList): PipelineBuilder = {

    // Create edges ignoring the duplicates.
    parents.foreach(makeEdge(_, stage, checkEdge = false))

    this
  }

  /** Add a parent to a node.
    *
    * @param stage The child stage.
    * @param parent The parent stage.
    * @tparam In Incoming type from 'start' stage.
    * @tparam Mid1 Intermediate type which is output for 'start' stage.
    * @tparam Mid2 Intermediate type which is output for 'end' stage.
    * @tparam Out Outgoing type for 'end' stage.
    * @return The builder instance.
    */
  def addParents[In <: Serializable with AnyRef,
                 Mid1 <: Serializable with AnyRef,
                 Mid2 <: Serializable with AnyRef,
                 Out <: Serializable with AnyRef](
      stage: Stage[In, Mid1],
      parent: Stage[Mid2, Out]): PipelineBuilder =
    addParents(stage, parent.inList)

  /** Builds a pipeline from the builder configuration.
    *
    * @throws EmptyPipelineException When no pipeline is defined (graph is empty).
    * @return A new [[Pipeline]].
    */
  def build(): Pipeline = {
    if (graph.isEmpty) throw EmptyPipelineException()

    // Correctly map and verify graph for every node.
    graph.nodes
      .foreach(
        _.asInstanceOf[Stage[Serializable with AnyRef,
                             Serializable with AnyRef]]
          .verifyGraph(graph))

    // This will verify the graph in terms of types.
    if (pipelineVerificationToggle) {
      graph.verify()
    } else {
      logger.warn(
        "Pipeline verification has been disabled manually. No type guarantee between stages can be given. Consider enabling it again.")
    }

    logger.info(
      s"Created pipeline with ${graph.nodes.size} nodes and ${graph.edges.size} edges.")
    logger.info(
      s"Buffer type: $bufferType, key manager: ${keyManager.getClass.getName}.")

    // Setup properties for pipeline.
    val props = PipelineProperties(bufferType,
                                   bufferProperties,
                                   keyManager,
                                   streamTimeCharacteristic)

    Pipeline(name, props, graph, stageProperties.toMap)
  }
}
