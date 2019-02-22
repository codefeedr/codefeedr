/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.buffer

import org.codefeedr.pipeline.{Pipeline, Stage}

import scala.reflect._
import scala.reflect.runtime.universe._

/** Factory for a Buffer.
  *
  * Creates a buffer that reflects the sink of a given object.
  *
  * When an in-buffer is necessary: relatedStage should be the parent of the stage.
  * When an out-buffer is necessary: relatedStage should be the actual stage that needs a sink.
  *
  * @param pipeline The Pipeline to create a Buffer for.
  * @param stage The Stage the Buffer should be connected to. It is necessary to know this beforehand, to resolve the types.
  * @param relatedStage The related-stage to which the stage either has to read or write to.
  * @param groupId Custom group id, to read from Kafka. Default is set to stage id.
  */
class BufferFactory[+In <: Serializable with AnyRef,
                    +Out <: Serializable with AnyRef,
                    +In1 <: Serializable with AnyRef,
                    +Out2 <: Serializable with AnyRef](
    pipeline: Pipeline,
    stage: Stage[In, Out],
    relatedStage: Stage[In1, Out2],
    groupId: String = null) {

  /** Creates a new buffer.
    *
    * @tparam T Type of the data within the Buffer.
    * @throws IllegalArgumentException When relatedStage is null.
    * @throws IllegalStateException When buffer could not be instantiated due to bad configuration.
    * @return A new Buffer of type T.
    */
  def create[T <: Serializable with AnyRef: ClassTag: TypeTag](): Buffer[T] = {
    require(
      relatedStage != null,
      "Buffer factory requires a sink object to determine buffer location")

    /** Get the id to read from or write to. */
    val subject = relatedStage.getContext.stageId
    val groupIdFinal =
      if (groupId != null) groupId else stage.getContext.stageId

    // Create the correct buffer.
    pipeline.bufferType match {
      case BufferType.Kafka => {
        val cleanedSubject = subject.replace("$", "-")
        new KafkaBuffer[T](pipeline,
                           pipeline.bufferProperties,
                           cleanedSubject,
                           groupIdFinal)
      }
      case x if BufferFactory.registry.exists(_._1 == x) => {
        val tt = typeTag[T]
        val ct = classTag[T]

        // Get Buffer from registry and instantiate using reflection.
        BufferFactory.registry
          .get(x)
          .get
          .runtimeClass
          .getConstructors()(0)
          .newInstance(pipeline, pipeline.bufferProperties, subject, ct, tt)
          .asInstanceOf[Buffer[T]]
      }
      case _ => {
        //Switch to Kafka.
        val cleanedSubject = subject.replace("$", "-")
        new KafkaBuffer[T](pipeline,
                           pipeline.bufferProperties,
                           cleanedSubject,
                           groupIdFinal)
      }
    }
  }
}

object BufferFactory {

  /** Reserved keywords for buffer names. */
  private val reserved = List(BufferType.Kafka)

  /** Map containing (type) references to Buffer by name. */
  private var registry: Map[String, Manifest[_ <: Buffer[_]]] = Map()

  /** Registers a new Buffer. This Buffer needs to be subclassed from [[Buffer]].
    *
    * In order to register your own SerDe:
    * 1. Create one by extending [[Buffer]]:
    * {{{
    * class YourBuffer[T <: Serializable with AnyRef: TypeTag: ClassTag]
    *     extends Buffer[T]
    * }}}
    * 2. Register your Buffer:
    * {{{
    * BufferFactory.register[Buffer[_]]("my_buffer")
    * }}}
    * 3. In the pipeline select your Buffer:
    * {{{
    *   pipelineBuilder.setBufferType("my_buffer")
    * }}}
    *
    * @param name Name of the Buffer. This needs to be unique. Reserved keywords are: Kafka, RabbitMQ.
    * @param ev Implicit Manifest of the class.
    * @tparam T Type of the buffer.
    * @throws IllegalArgumentException Thrown when name is not unique/already registered.
    */
  def register[T <: Buffer[_]](name: String)(implicit ev: Manifest[T]) = {
    if (reserved.contains(name) || registry.exists(_._1 == name))
      throw new IllegalArgumentException("Buffer already exists.")

    // Add manifest to registry
    registry += (name -> ev)
  }
}
