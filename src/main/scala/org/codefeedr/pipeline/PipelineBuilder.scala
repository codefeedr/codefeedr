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

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.{DirectedAcyclicGraph, Properties}
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.PipelineType.PipelineType
import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.pipeline.buffer.BufferType.BufferType

import scala.reflect.ClassTag

class PipelineBuilder() {
  /** Type of buffer used in the pipeline */
  protected var bufferType: BufferType = BufferType.None

  /** Type of the pipeline graph */
  protected var pipelineType: PipelineType = PipelineType.Sequential

  /** Properties of the buffer */
  var bufferProperties = new Properties()

  /** Pipeline properties */
  var properties = new Properties()

  /** Key manager */
  protected var keyManager: KeyManager = _

  /** Graph of the pipeline */
  protected[pipeline] var graph = new DirectedAcyclicGraph()

  /** Last inserted pipeline obejct, used to convert sequential to dag. */
  private var lastObject: AnyRef = _


  def getBufferType: BufferType = {
    bufferType
  }

  def setBufferType(bufferType: BufferType): PipelineBuilder = {
    this.bufferType = bufferType

    this
  }

  def getPipelineType: PipelineType= {
    pipelineType
  }

  def setPipelineType(pipelineType: PipelineType): PipelineBuilder = {
    if (pipelineType == PipelineType.Sequential && this.pipelineType == PipelineType.DAG) {
      if (!graph.isSequential) {
        throw new IllegalStateException("The current non-sequential pipeline can't be turned into a sequential pipeline")
      }

      lastObject = graph.lastInSequence.get
    }

    this.pipelineType = pipelineType

    this
  }

  def setProperty(key: String, value: String): PipelineBuilder = {
    properties = properties.set(key, value)

    this
  }

  def setBufferProperty(key: String, value: String): PipelineBuilder = {
    bufferProperties = bufferProperties.set(key, value)

    this
  }

  def setKeyManager(km: KeyManager): PipelineBuilder = {
    keyManager = km

    this
  }

  /**
    * Append a node to the sequential pipeline.
    */
  def append[U <: PipelineItem, V <: PipelineItem](item: PipelineObject[U, V]): PipelineBuilder = {
    if (pipelineType != PipelineType.Sequential) {
      throw new IllegalStateException("Can't append node to non-sequential pipeline")
    }

    if (graph.hasNode(item)) {
      throw new IllegalArgumentException("Item already in sequence.")
    }

    graph = graph.addNode(item)

    if (lastObject != null) {
      graph = graph.addEdge(lastObject, item)
    }
    lastObject = item

    this
  }

  def append[U <: PipelineItem : ClassTag : Manifest : FromRecord, V <: PipelineItem : ClassTag : Manifest : FromRecord](trans : DataStream[U] => DataStream[V]) : PipelineBuilder = {
    val pipelineItem = new PipelineObject[U, V]() {
      override def transform(source: DataStream[U]): DataStream[V] = trans(source)
    }

    append(pipelineItem)
  }

  def append[U <: PipelineItem : ClassTag : Manifest : FromRecord](trans : DataStream[U] => Any) : PipelineBuilder = {
    val pipelineItem = new Job[U]() {
      override def main(source: DataStream[U]): Unit = trans(source)
    }

    append(pipelineItem)
  }

  private def makeEdge[U <: PipelineItem, V <: PipelineItem, X <: PipelineItem, Y <: PipelineItem](from: PipelineObject[U, V], to: PipelineObject[X, Y]): Unit = {
    if (pipelineType != PipelineType.DAG) {
      if (!graph.isEmpty) {
        throw new IllegalStateException("Can't append node to non-sequential pipeline")
      }

      pipelineType = PipelineType.DAG
    }

    if (!graph.hasNode(from)) {
      graph = graph.addNode(from)
    }

    if (!graph.hasNode(to)) {
      graph = graph.addNode(to)
    }

    if (graph.hasEdge(from, to)) {
      throw new IllegalArgumentException("Edge in graph already exists")
    }

    graph = graph.addEdge(from, to)
  }

  /**
    * Create an edge between two sources in a DAG pipeline. The 'to' must not already have a parent.
    *
    * If the graph is not configured yet (has no nodes), the graph is switched to a DAG automatically. If it was
    * already configured as sequential, it will throw an illegal state exception.
    */
  def edge[U <: PipelineItem, V <: PipelineItem, X <: PipelineItem, Y <: PipelineItem](from: PipelineObject[U, V], to: PipelineObject[X, Y]): PipelineBuilder = {
    if (graph.getParents(to).nonEmpty) {
      throw new IllegalArgumentException("Can't add main edge to node with already any parent")
    }

    makeEdge(from, to)

    this
  }

  /**
    * Create an edge between two sources in a DAG pipeline. The 'to' can already have a parent.
    *
    * If the graph is not configured yet (has no nodes), the graph is switched to a DAG automatically. If it was
    * already configured as sequential, it will throw an illegal state exception.
    */
  def extraEdge[U <: PipelineItem, V <: PipelineItem, X <: PipelineItem, Y <: PipelineItem](from: PipelineObject[U, V], to: PipelineObject[X, Y]): PipelineBuilder = {
    if (graph.getParents(to).isEmpty) {
      throw new IllegalArgumentException("Can't add extra edge to node with no main parent")
    }

    makeEdge(from, to)

    this
  }

  /**
    * Add multiple parents in given ordered list.
    *
    * @param obj Node
    * @param parents Parents
    * @tparam U Node In
    * @tparam V Node Out
    * @return Builder
    */
  def addParents[U <: PipelineItem, V <: PipelineItem](obj: PipelineObject[U, V], parents: PipelineObjectList): PipelineBuilder = {
    if (!graph.hasNode(obj)) {
      graph = graph.addNode(obj)
    }

    for (item <- parents) {
      if (!graph.hasNode(item)) {
        graph = graph.addNode(item)
      }

      graph = graph.addEdge(item, obj)
    }

    this
  }

  /**
    * Add a parent.
    *
    * @param obj Node
    * @param parent Parent
    * @return
    */
  def addParents[U <: PipelineItem, V <: PipelineItem, X <: PipelineItem, Y <: PipelineItem](obj: PipelineObject[U, V], parent: PipelineObject[X, Y]): PipelineBuilder =
    addParents(obj, parent.inList)

  /**
    * Build a pipeline from the builder configuration
    *
    * @throws EmptyPipelineException When no pipeline is defined
    * @return Pipeline
    */
  def build(): Pipeline = {
    if (graph.isEmpty) {
      throw EmptyPipelineException()
    }

    graph.nodes.foreach(_.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]].verifyGraph(graph))

    Pipeline(bufferType, bufferProperties, graph , properties, keyManager)
  }
}
