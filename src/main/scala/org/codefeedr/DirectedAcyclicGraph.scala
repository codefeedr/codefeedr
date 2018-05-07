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
    if (!hasNode(from) || !hasNode(to)) {
      throw new IllegalArgumentException("One or more nodes for edge do not exist")
    }

    // If to can reach from already adding this edge will cause a cycle
    if (canReach(to, from)) {
      throw new IllegalStateException("Given edge causes a cycle in the DAG")
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

  protected def hasAnyEdge(node: AnyRef): Boolean = {
    for (n <- nodes) {
      if (hasEdge(node, n) || hasEdge(n, node)) {
        return true
      }
    }
    false
  }

  def withoutOrphans: DirectedAcyclicGraph = {
    val newNodes = nodes.filter(n => hasAnyEdge(n))
    new DirectedAcyclicGraph(newNodes, edges)
  }

  def getParents(node: AnyRef): Set[AnyRef] = {
    nodes.filter(n => hasEdge(n, node))
  }

  def getChildren(node: AnyRef): Set[AnyRef] = {
    nodes.filter(n => hasEdge(node, n))
  }

  def isSequential: Boolean = {
    var node = nodes.head

    // Must have at most 1 parent, and each node before it too
    while (node != null) {
      val parents = getParents(node)
      if (parents.size > 1) {
        return false
      }

      node = if (parents.isEmpty) null else parents.head
    }

    // Must have at most 1 child, and each child too
    node = nodes.head
    while (node != null) {
      val children = getChildren(node)
      if (children.size > 1) {
        return false
      }

      node = if (children.isEmpty) null else children.head
    }

    true
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case dag: DirectedAcyclicGraph => this.nodes == dag.nodes && this.edges == dag.edges
      case _ => false
    }
  }
}

object DirectedAcyclicGraph {
  case class Edge(from: AnyRef, to: AnyRef)
}