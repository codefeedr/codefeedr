package org.codefeedr.pipeline

object Runtime extends Enumeration {
  type Runtime = Value
  val Mock, Local, Cluster = Value
}
