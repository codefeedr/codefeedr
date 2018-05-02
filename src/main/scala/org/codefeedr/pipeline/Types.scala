package org.codefeedr.pipeline

case class NoType() extends PipelinedItem

trait PipelinedItem extends Serializable