package org.codefeedr.buffer

import org.codefeedr.stages.InputStage

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/** Fake stage to link up already running stages. */
abstract class FakeStage[T <: Serializable with AnyRef: ClassTag: TypeTag](
    stageId: String)
    extends InputStage[T](Some(stageId)) {}
