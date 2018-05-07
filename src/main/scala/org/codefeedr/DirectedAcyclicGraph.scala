package org.codefeedr

/**
  * A directed acyclic graph
  *
  * @todo Find a way to replace AnyRef with a tparam T
  *
  * @param nodes List of nodes
  * @param edges List of edges
  */
class DirectedAcyclicGraph(val nodes: Set[AnyRef] = Set(), val edges: Set[DirectedAcyclicGraph.Edge] = Set()) {

  def isEmpty: Boolean = nodes.isEmpty

  def hasNode(node: AnyRef): Boolean = nodes.contains(node)

  def addNode(node: AnyRef): DirectedAcyclicGraph = new DirectedAcyclicGraph(nodes + node, edges)

  def hasEdge(from: AnyRef, to: AnyRef): Boolean = {
    for (edge <- edges) {
      if (edge.from == from && edge.to == to) {
        return true
      }
    }

    false
  }

  def addEdge(from: AnyRef, to: AnyRef): DirectedAcyclicGraph = {
    // If to can reach from already adding this edge will cause a cycle
    if (canReach(to, from)) {
      throw new IllegalArgumentException("Given edge causes a cycle in the DAG")
    }

    new DirectedAcyclicGraph(nodes, edges + DirectedAcyclicGraph.Edge(from, to))
  }

  def canReach(from: AnyRef, to: AnyRef): Boolean = {
    // Direct edge
    if (hasEdge(from, to)) {
      return true
    }

    for (node <- nodes) {
      // Find neighbour nodes and do recursive call
      if (hasEdge(from, node) && canReach(node, to)) {
        return true
      }
    }

    false
  }

  def withoutOrphans: DirectedAcyclicGraph = {
    // TODO; find if everything is connected

    this
  }

  def getParents(node: AnyRef): Set[AnyRef] = {
    null
  }

  def isSequential: Boolean = {
    // TODO

    true
  }
}

object DirectedAcyclicGraph {
  case class Edge(from: AnyRef, to: AnyRef)
}