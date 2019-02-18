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
package org.codefeedr

import org.codefeedr.buffer.{Buffer, BufferFactory}
import org.codefeedr.buffer.serialization.{AbstractSerde, Serializer}

/** Some helper functions to make your life easier **/
package object api {

  /** Registers a new SerDe. This SerDe needs to be subclassed from [[AbstractSerde]].
    *
    * @param name Name of the SerDe. This needs to be unique. Reserved keywords are: JSON, BSON, KRYO.
    * @param ev Implicit Manifest of the class.
    * @tparam T Type of the serializer.
    * @throws IllegalArgumentException Thrown when name is not unique/already registered.
    */
  def registerSerializer[T <: AbstractSerde[_]](name: String)(
      implicit ev: Manifest[T]) =
    Serializer.register[T](name)

  /** Registers a new Buffer. This Buffer needs to be subclassed from [[Buffer]].
    *
    * @param name Name of the Buffer. This needs to be unique. Reserved keywords are: Kafka, RabbitMQ.
    * @param ev Implicit Manifest of the class.
    * @tparam T Type of the buffer.
    * @throws IllegalArgumentException Thrown when name is not unique/already registered.
    */
  def registerBuffer[T <: Buffer[_]](name: String)(implicit ev: Manifest[T]) =
    BufferFactory.register[T](name)
}
