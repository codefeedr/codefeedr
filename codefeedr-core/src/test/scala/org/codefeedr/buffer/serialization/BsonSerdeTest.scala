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

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.scalatest.{BeforeAndAfter, FunSuite}

class BsonSerdeTest extends FunSuite with BeforeAndAfter {

  private case class SimpleCaseClass(str: String, i: Int)
  private case class ComplexCaseClass(str: String, i : Option[Int], l : List[Date])

  private var serde : BsonSerde[SimpleCaseClass] = _
  private var serde2 : BsonSerde[ComplexCaseClass] = _

  before {
    serde = BsonSerde[SimpleCaseClass]
    serde2 = BsonSerde[ComplexCaseClass]
  }

  test ("Deserializes complex serialized values") {
    val value = ComplexCaseClass("hello", Some(42), List(new Date, new Date))

    val serialized = serde2.serialize(value)
    val deserialized = serde2.deserialize(serialized)

    assert(deserialized == value)
  }

  test ("Deserializes complex serialized values 2") {
    val value = ComplexCaseClass("hello", None, List())

    val serialized = serde2.serialize(value)
    val deserialized = serde2.deserialize(serialized)

    assert(deserialized == value)
  }

  test("Deserializes simple serialized values") {
    val value = SimpleCaseClass("hello", 42)

    val serialized = serde.serialize(value)
    val deserialized = serde.deserialize(serialized)

    assert(deserialized == value)
  }

  test("Simple typeinformation check") {
    val typeInformation = TypeInformation.of(classOf[SimpleCaseClass])

    assert(typeInformation == serde.getProducedType)
  }

}
