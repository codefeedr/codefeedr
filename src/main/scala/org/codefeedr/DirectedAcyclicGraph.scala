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
package org.codefeedr

/**
  * A directed acyclic graph
  *
  * A graph that is always acyclic.
  *
  * @todo Find a way to replace AnyRef with a tparam T
  *
  * @param nodes List of nodes
  * @param edges List of edges
  */
final class DirectedAcyclicGraph(val nodes: Vector[AnyRef] = Vector(), val edges: Vector[DirectedAcyclicGraph.Edge] = Vector()) {

  /**
    * Get whether the collection is empty
    * @return true when there are no nodes
    */
  def isEmpty: Boolean = nodes.isEmpty

  /**
    * Get whether given node is in graph.
    * @param node Node
    * @return true if in nodes
    */
  def hasNode(node: AnyRef): Boolean = nodes.contains(node)

  /**
    * Add given node to the graph. Nodes already in the graph will not be added again.
    * @param node
    * @return A new graph with the node included
    */
  def addNode(node: AnyRef): DirectedAcyclicGraph =
    new DirectedAcyclicGraph(nodes :+ node, edges)

  /**
    * Get whethere there is an edge directly from the first to the second node.
    * @param from A node
    * @param to A node
    * @return true when an edge from 'from' to 'to'.
    */
  def hasEdge(from: AnyRef, to: AnyRef): Boolean =
    edges.exists(edge => edge.from == from && edge.to == to)

  /**
    * Add an edge.
    *
    * @param from A node to start the edge from.
    * @param to A node the start the edge at
    * @throws IllegalArgumentException When either node is not in the graph or when the given edge causes a cycle.
    * @return A new graph with the edge included
    */
  def addEdge(from: AnyRef, to: AnyRef, main: Boolean = false): DirectedAcyclicGraph = {
    if (!hasNode(from) || !hasNode(to)) {
      throw new IllegalArgumentException("One or more nodes for edge do not exist")
    }

    if (main && getMainParent(to).isDefined) {
      throw new IllegalArgumentException("Can't add second main parent to node")
    }

    // If to can reach from already adding this edge will cause a cycle
    if (canReach(to, from)) {
      throw new IllegalArgumentException("Given edge causes a cycle in the DAG")
    }

    val edge = DirectedAcyclicGraph.Edge(from, to, main)
    if (edges.contains(edge))
      this
    else
      new DirectedAcyclicGraph(nodes, edges :+ edge)
  }

  /**
    * Get the value at the given edge, if any.
    *
    * @param from From node
    * @param to To node
    * @return Optional value
    */
  def getEdge(from: AnyRef, to: AnyRef): Option[Boolean] = {
    edges.find(edge => edge.from == from && edge.to == to) match {
      case Some(e: DirectedAcyclicGraph.Edge) => Some(e.main)
      case _ => None
    }
  }

  /**
    * Decide if there is a path from one node to the other
    * @param from A node
    * @param to Another node
    * @return true when there is a path.
    */
  def canReach(from: AnyRef, to: AnyRef): Boolean = {
    // Direct edge
    if (hasEdge(from, to)) {
      return true
    }

    nodes.exists(node => hasEdge(from, node) && canReach(node, to))
  }

  /**
    * Determine whether the node has any edge at all.
    *
    * @param node Node
    * @return true when it has any edge
    */
  protected def hasAnyEdge(node: AnyRef): Boolean =
    nodes.exists(n => hasEdge(node, n) || hasEdge(n, node))

  /**
    * Get a copy of the graph with all orphans removed.
    *
    * Orphans are nodes without any edges.
    *
    * @return a graph
    */
  def withoutOrphans: DirectedAcyclicGraph = {
    val newNodes = nodes.filter(n => hasAnyEdge(n))
    new DirectedAcyclicGraph(newNodes, edges)
  }

  /**
    * Get a set of parents for given node.
    *
    * @param node Node
    * @return A set which can be empty.
    */
  def getParents(node: AnyRef): Vector[AnyRef] =
    nodes.filter(n => hasEdge(n, node))

  /**
    * Get the parent that is designated as main parent.
    *
    * @param node Node
    * @return Optional parent
    */
  def getMainParent(node: AnyRef): Option[AnyRef] =
    nodes.filter(n => getEdge(n, node) getOrElse false).lastOption

  /**
    * Get a set of children for given node
    *
    * @param node Node
    * @return A set which can be empty
    */
  def getChildren(node: AnyRef): Vector[AnyRef] =
    nodes.filter(n => hasEdge(node, n))

  /**
    * Decide whether the graph is sequential.
    *
    * Sequential means that there is no node with multiple parents or
    * children: the whole set of nodes is a connected line.
    *
    * @return true when the graph is sequential
    */
  def isSequential: Boolean =
    nodes.isEmpty || (!nodes.exists(n => getParents(n).size > 1 || getChildren(n).size > 1) && nodes.size - 1 == edges.size)

  /**
    * Find the last node, assuming this graph is sequential.
    *
    * @return node or null
    */
  def lastInSequence: Option[AnyRef] = {
    if (!isSequential || nodes.isEmpty) {
      return None
    }

    var node = nodes.head
    var lastNode: AnyRef = null

    do {
      val edge = edges.find(edge => edge.from == node)
      if (edge.isEmpty) {
        lastNode = node
      } else {
        node = edge.get.to
      }
    } while (lastNode == null)

    Option(lastNode)
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dag: DirectedAcyclicGraph => this.nodes == dag.nodes && this.edges == dag.edges
      case _ => false
    }
  }
}

object DirectedAcyclicGraph {

  /**
    * An edge in the graph
    * @param from Node
    * @param to Node
    */
  case class Edge(from: AnyRef, to: AnyRef, main: Boolean)
}