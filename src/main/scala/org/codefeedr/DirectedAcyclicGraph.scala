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
class DirectedAcyclicGraph(val nodes: Set[AnyRef] = Set(), val edges: Set[DirectedAcyclicGraph.Edge] = Set()) {

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
  def addNode(node: AnyRef): DirectedAcyclicGraph = new DirectedAcyclicGraph(nodes + node, edges)

  /**
    * Get whethere there is an edge directly from the first to the second node.
    * @param from A node
    * @param to A node
    * @return true when an edge from 'from' to 'to'.
    */
  def hasEdge(from: AnyRef, to: AnyRef): Boolean = {
    for (edge <- edges) {
      if (edge.from == from && edge.to == to) {
        return true
      }
    }

    false
  }

  /**
    * Add an edge.
    *
    * @param from A node to start the edge from.
    * @param to A node the start the edge at
    * @throws IllegalArgumentException When either node is not in the graph or when the given edge causes a cycle.
    * @return A new graph with the edge included
    */
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
  def getParents(node: AnyRef): Set[AnyRef] = {
    nodes.filter(n => hasEdge(n, node))
  }

  /**
    * Get a set of children for given node
    *
    * @param node Node
    * @return A set which can be empty
    */
  def getChildren(node: AnyRef): Set[AnyRef] = {
    nodes.filter(n => hasEdge(node, n))
  }

  /**
    * Decide whether the graph is sequential.
    *
    * Sequential means that there is no node with multiple parents or
    * children: the whole set of nodes is a connected line.
    *
    * @return true when the graph is sequential
    */
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

  /**
    * An edge in the graph
    * @param from Node
    * @param to Node
    */
  case class Edge(from: AnyRef, to: AnyRef)
}