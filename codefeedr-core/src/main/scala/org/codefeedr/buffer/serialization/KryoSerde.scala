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
 */

package org.codefeedr.buffer.serialization

import java.io.ByteArrayOutputStream

import com.twitter.chill.{Input, Output, ScalaKryoInstantiator}

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

/**
  * Serializer using the Kryo serialization framework for fast and small results.
  *
  * Cannot be used on innerclasses
  *
  * @tparam T
  */
class KryoSerde[T <: AnyRef : TypeTag : ClassTag] extends AbstractSerde[T]{

  /**
    * Serializes a (generic) element using Kryo.
    *
    * @param element the element to serialized.
    * @return a serialized byte array.
    */
  override def serialize(element: T): Array[Byte] = {
    val buffer = new Array[Byte](KryoSerde.BUFFER_SIZE)
    val output = new Output(buffer)
    kryo.writeObject(output, element)

    buffer
  }


  /**
    * Deserializes a Kryo message
    *
    * @param message the message to deserialized.
    * @return a deserialized case class.
    */
  override def deserialize(message: Array[Byte]): T =
    kryo.readObject(new Input(message), inputClassType)

  private def kryo = {
    val inst = new ScalaKryoInstantiator
    inst.setRegistrationRequired(false)
    inst.newKryo()
  }
}

object KryoSerde {
  val BUFFER_SIZE = 4096

  def apply[T <: AnyRef : ClassTag : TypeTag]: KryoSerde[T] = new KryoSerde[T]()
}