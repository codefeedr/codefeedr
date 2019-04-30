package org.codefeedr.buffer

import org.codefeedr.stages.InputStage

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

abstract class FakeBuffer[T <: Serializable with AnyRef: ClassTag: TypeTag]
    extends InputStage[T] {}
