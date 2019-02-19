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

import java.lang.reflect.ParameterizedType
import org.codefeedr.stages.InputStage
import org.codefeedr.stages.OutputStage

/** Links two nodes. */
final case class Edge(from: AnyRef, to: AnyRef)

/** A directed acyclic graph.
  * Every [[Pipeline]] is enforced into a DAG, so that data cannot flow in a loop.
  * [[Stage]]'s are nodes in this graph whereas edges represent data flow using a [[org.codefeedr.buffer.Buffer]].
  *
  * This class is immutable so that graph can be build in a functional manner:
  * {{{
  *   val dag = new DirectedAcyclicGraph()
  *     .addNode(nodeOne)
  *     .addNode(nodeTwo)
  *     .addEdge(nodeOne, nodeTwo)
  * }}}
  *
  * @param nodes List of nodes to build the graph from.
  * @param edges List of edges to build the graph from.
  */
final class DirectedAcyclicGraph(val nodes: Vector[AnyRef] = Vector(),
                                 val edges: Vector[Edge] = Vector()) {

  /** Check whether a graph is empty.
    *
    * @return True when there are no nodes.
    */
  def isEmpty: Boolean = nodes.isEmpty

  /** Check whether given node is in the graph.
    *
    * @param node Node to verify.
    * @return True if in node is in graph.
    */
  def hasNode(node: AnyRef): Boolean = nodes.contains(node)

  /** Add given node to the graph.
    * Note: Nodes already in the graph will not be added again.
    *
    * @param node Node to add.
    * @return A new graph with the node included.
    */
  def addNode(node: AnyRef): DirectedAcyclicGraph =
    new DirectedAcyclicGraph(nodes :+ node, edges)

  /** Check whether an edge exists between two nodes.
    *
    * @param from The 'from' node.
    * @param to The 'to' node.
    * @return True when an edge from 'from' to 'to'.
    */
  def hasEdge(from: AnyRef, to: AnyRef): Boolean =
    edges.exists(edge => edge.from == from && edge.to == to)

  /** Adds an edge between two nodes in given graph.
    *
    * @param from The 'start' node.
    * @param to The 'end' node.
    * @throws IllegalArgumentException When either node is not in the graph or when the given edge causes a cycle.
    * @return A new graph with the edge included
    */
  def addEdge(from: AnyRef, to: AnyRef): DirectedAcyclicGraph = {
    if (!hasNode(from) || !hasNode(to)) {
      throw new IllegalArgumentException(
        "One or more nodes for edge do not exist")
    }

    // If to can reach from already adding this edge will cause a cycle.
    if (canReach(to, from)) {
      throw new IllegalArgumentException(
        "Given edge causes a cycle in the DAG.")
    }

    // Create new edge.
    val edge = Edge(from, to)

    // If edge already exists, return same DAG else a new one.
    if (edges.contains(edge))
      this
    else
      new DirectedAcyclicGraph(nodes, edges :+ edge)
  }

  /** Check whether there is a path from one node to the other.
    *
    * @param from The 'from' node.
    * @param to The 'to' node.
    * @return true when there is a path.
    */
  def canReach(from: AnyRef, to: AnyRef): Boolean = {
    // True if there is a direct path.
    if (hasEdge(from, to)) {
      return true
    }

    // Recursively find a path.
    nodes.exists(node => hasEdge(from, node) && canReach(node, to))
  }

  /** Check whether the node has any edge at all.
    *
    * @param node The node to check.
    * @return True when it has more than zero edges.
    */
  def hasAnyEdge(node: AnyRef): Boolean =
    nodes.exists(n => hasEdge(node, n) || hasEdge(n, node))

  /** Get a copy of the graph with all orphans removed.
    * Orphans are nodes without edges.
    *
    * @return The graph without orphan nodes.
    */
  def withoutOrphans: DirectedAcyclicGraph = {

    // Get all nodes with at least 1 edge.
    val newNodes = nodes.filter(n => hasAnyEdge(n))

    // Return a new graph.
    new DirectedAcyclicGraph(newNodes, edges)
  }

  /** Get a parents of a node.
    *
    * @param node Node to get parents from.
    * @return A set with the parents of the node. Note: can be the empty set.
    */
  def getParents(node: AnyRef): Vector[AnyRef] =
    nodes.filter(n => hasEdge(n, node))

  /** Get the parent that is designated as first/primary parent.
    * This is the node which edges is found first.
    *
    * @param node Node to get first/primary parent from.
    * @return The parent node. Note: Optional since the parent can be non-existing.
    */
  def getFirstParent(node: AnyRef): Option[AnyRef] = {
    val parents = getParents(node)
    if (parents.nonEmpty) Some(parents(0)) else None
  }

  /** Get children of a node.
    *
    * @param node Node to get children from.
    * @return A set with the children of the node. Note: can be the empty set.
    */
  def getChildren(node: AnyRef): Vector[AnyRef] =
    nodes.filter(n => hasEdge(node, n))

  /** Check whether the graph is sequential.
    *
    * Sequential means that there is no node with multiple parents or
    * children. The whole set of nodes is a connected line. An empty graph is also sequential.
    *
    * @return True when the graph is sequential.
    */
  def isSequential: Boolean =
    nodes.isEmpty || (!nodes.exists(n =>
      getParents(n).size > 1 || getChildren(n).size > 1) && nodes.size - 1 == edges.size)

  /** Find the last node, assuming this graph is sequential.
    *
    * @return The last node or None if the graph is not sequential or empty.
    */
  def lastInSequence: Option[AnyRef] = {
    if (!isSequential || nodes.isEmpty) {
      return None
    }

    nodes.find(node => !nodes.exists(toNode => hasEdge(node, toNode)))
  }

  /**
    *
    */
  def verifyGraph() = {
    // Get all input stages nodes.
    val nodesInputStages = nodes.filter(x => x.isInstanceOf[InputStage[_]])

    // Get all output stages nodes.
    val nodesOutputStages = nodes.filter(x => x.isInstanceOf[OutputStage[_]])

    // If there are InputStages which have incoming edges, something is going wrong.
    if (nodesInputStages.exists(getParents(_).size > 0)) {
      throw new InvalidPipelineException(
        "Check your pipeline, there are InputStages which have incoming edges. This is not possible.")
    }

    // If there are OutputStages which have outgoing edges, something is going wrong.
    if (nodesOutputStages.exists(getChildren(_).size > 0)) {
      throw new InvalidPipelineException(
        "Check your pipeline, there are OutputStages which have outgoing edges. This is not possible.")
    }

    // Verify types.
    nodes
      .filterNot(x => x.isInstanceOf[InputStage[_]]) // We ignore input stages, they don't have incoming edges.
      .foreach { n =>
        // Get the name of this stage.
        val stageName = n.getClass.getName

        // Get output type of this stage.
        val stageOutputType = n
          .asInstanceOf[Stage[_, _]]
          .getOutType
          .getName

        // Get all the input types of this stage as a set.
        val inputTypes = n.getClass.getGenericSuperclass
          .asInstanceOf[ParameterizedType]
          .getActualTypeArguments
          .map(_.getTypeName)
          .filter(_ != stageOutputType)

        // Get all the types of the incoming edges (which is the output type of the other stage).
        val edgesOutputTypes =
          getParents(n)
            .map(_.asInstanceOf[Stage[_, _]].getOutType.getName)

        /**
          * The amount of inputTypes should be equal to the amount of incoming edges.
          * This is actually also checked in the [[StageN]] class.
          */
        if (inputTypes.size != edgesOutputTypes.size) {
          throw new StageTypesIncompatibleException(
            s"The stage $stageName requires ${inputTypes.size} input types, but retrieves ${edgesOutputTypes.size} input types.")
        }

        // The set of both the input types and the 'output' types of all incoming edges should be equal.
        if (inputTypes.toSet != edgesOutputTypes.toSet) {
          throw new StageTypesIncompatibleException(
            s"The stage $stageName has incompatible types, it expects [${inputTypes.mkString(
              ", ")}] but instead got [${edgesOutputTypes.mkString(", ")}].")
        }
      }

  }

  /** Equality check for a DAG.
    *
    * @param obj Object to compare with.
    * @return True if equal.
    */
  override def equals(obj: Any): Boolean = {
    obj match {
      case dag: DirectedAcyclicGraph =>
        this.nodes == dag.nodes && this.edges == dag.edges
      case _ => false
    }
  }
}
