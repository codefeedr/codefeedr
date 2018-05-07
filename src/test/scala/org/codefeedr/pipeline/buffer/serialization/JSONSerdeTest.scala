package org.codefeedr.pipeline.buffer.serialization

import org.scalatest.{BeforeAndAfter, FunSuite}



class JSONSerdeTest extends FunSuite with BeforeAndAfter {
  private case class SimpleCaseClass(str: String, i: Int)

  private var serde : JSONSerde[SimpleCaseClass] = null

  before {
    serde = new JSONSerde[SimpleCaseClass]()
  }

  test("Serializes simple values") {
    val value = SimpleCaseClass("hello", 42)

    val byteArray = serde.serialize(value)

    val expected = """{"str":"hello","i":42}"""

    assert(byteArray sameElements expected.getBytes)
  }

  test("Simple case class equality") {
    val a = SimpleCaseClass("hello", 42)
    val b = SimpleCaseClass("hello", 42)

    assert(a == b)
  }

  test("Deserializes simple values") {
    val value = """{"str":"hello","i":42}"""
    val deserialized = serde.deserialize(value.getBytes)
    val expected = SimpleCaseClass("hello", 42)

    assert(deserialized == expected)
  }

  test("Deserializes simple serialized values") {
    val value = SimpleCaseClass("hello", 42)

    val serialized = serde.serialize(value)
    val deserialized = serde.deserialize(serialized)

    assert(deserialized == value)
  }

}
