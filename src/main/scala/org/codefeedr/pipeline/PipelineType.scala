package org.codefeedr.pipeline

object PipelineType extends Enumeration {
  type PipelineType = Value
  val Sequential, DAG = Value
}
