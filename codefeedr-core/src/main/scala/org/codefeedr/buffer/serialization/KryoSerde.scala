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

import com.twitter.chill.{Input, KryoPool, Output, ScalaKryoInstantiator}

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

class KryoSerde[T <: AnyRef : TypeTag : ClassTag] extends AbstractSerde[T]{

  /**
    * Serializes a (generic) element using Kryo.
    *
    * @param element the element to serialized.
    * @return a serialized byte array.
    */
  override def serialize(element: T): Array[Byte] = {
//    ScalaKryoInstantiator.defaultPool.toBytesWithClass(element)
    val buffer = new Array[Byte](4096)
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
  override def deserialize(message: Array[Byte]): T = {
    val input = new Input(message)

    kryo.readObject(input, inputClassType)

//    ScalaKryoInstantiator.defaultPool.fromBytes(message).asInstanceOf[T]
  }

  def kryo = {
    val inst = new ScalaKryoInstantiator
    inst.setRegistrationRequired(false)
    inst.newKryo()
  }
}

object KryoSerde {
  def apply[T <: AnyRef : ClassTag : TypeTag]: KryoSerde[T] = new KryoSerde[T]()
}