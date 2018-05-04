package org.codefeedr.pipeline.buffer.serialization

import org.scalatest.FunSuite



class JSONSerializationSchemaTest extends FunSuite {
  private case class SimpleCaseClass(str: String, i: Int)

  test("Serializes simple values") {
    val serializer = new JSONSerializationSchema[SimpleCaseClass]
    val value = SimpleCaseClass("hello", 42)

    val byteArray = serializer.serialize(value)

    val expected = """{"str":"hello","i":42}"""

    assert(byteArray sameElements expected.getBytes)
  }

  test("Simple case class equality") {
    val a = SimpleCaseClass("hello", 42)
    val b = SimpleCaseClass("hello", 42)

    assert(a == b)
  }

  test("Deserializes simple values") {
    val deserializer = new JSONDeserializationSchema[SimpleCaseClass]

    val value = """{"str":"hello","i":42}"""
    val deserialized = deserializer.deserialize(value.getBytes)
    val expected = SimpleCaseClass("hello", 42)

    assert(deserialized == expected)
  }

  test("Deserializes simple serialized values") {
    val serializer = new JSONSerializationSchema[SimpleCaseClass]
    val deserializer = new JSONDeserializationSchema[SimpleCaseClass]
    val value = SimpleCaseClass("hello", 42)

    val serialized = serializer.serialize(value)
    val deserialized = deserializer.deserialize(serialized)

    assert(deserialized == value)
  }

}
