package org.codefeedr.pipeline

trait Job[+T <: PipelinedItem] extends PipelineObject[T, NoType] {

}
