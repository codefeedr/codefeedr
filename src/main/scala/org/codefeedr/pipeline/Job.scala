package org.codefeedr.pipeline

abstract class Job[T <: PipelinedItem] extends PipelineObject[T, NoType] {

}
