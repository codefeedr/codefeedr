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
package org.codefeedr.buffer.serialization

import com.mongodb.BasicDBObject
import org.bson._
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class BsonSerde[T  <: Serializable with AnyRef : TypeTag : ClassTag] extends AbstractSerde[T] {

  implicit lazy val formats = Serialization.formats(NoTypeHints) ++ JavaTimeSerializers.all

  lazy val encoder = new BasicBSONEncoder()

  /**
    * Serializes an element using Bson.
    * @param element the element to serialize.
    * @return the serialized byte array.
    */
  override def serialize(element: T): Array[Byte] = {
    val json = Serialization.write(element)(formats)
    encoder.encode(BasicDBObject.parse(json))
  }

  /**
    * Deserializes an element using Bson.
    * @param message the message to deserialize.
    * @return a deserialized case class.
    */
  override def deserialize(message: Array[Byte]): T = {
    val json = new RawBsonDocument(message).toJson
    Serialization.read[T](json)
  }
}

//companion object to simply instantiate bson serde
object BsonSerde {
  def apply[T  <: Serializable with AnyRef : TypeTag : ClassTag]: BsonSerde[T] = new BsonSerde[T]()
}
