package org.codefeedr.pipeline

import org.codefeedr.{DirectedAcyclicGraph, Properties}
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.PipelineType.PipelineType
import org.codefeedr.pipeline.buffer.BufferType
import org.codefeedr.pipeline.buffer.BufferType.BufferType

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
      graph = graph.addEdge(lastObject, item, true)
    }
    lastObject = item

    this
  }

  private def makeEdge[U <: PipelineItem, V <: PipelineItem, X <: PipelineItem, Y <: PipelineItem](from: PipelineObject[U, V], to: PipelineObject[X, Y], main: Boolean): Unit = {
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

    graph = graph.addEdge(from, to, main)
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

    makeEdge(from, to, main = true)

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

    makeEdge(from, to, main = false)

    this
  }

  /**
    * Build a pipeline from the builder configuration
    *
    * @throws EmptyPipelineException When no pipeline is defined
    * @return Pipeline
    */
  def build(): Pipeline = {
    if (this.graph.isEmpty) {
      throw EmptyPipelineException()
    }

    Pipeline(bufferType, bufferProperties, graph , properties, keyManager)
  }
}
