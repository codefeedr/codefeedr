package org.codefeedr.buffer.serialization

import org.scalatest.FunSuite

class SerializerTest extends FunSuite {

  case class Item()

  test("Should recognise JSON") {
    val serde = Serializer.getSerde[Item](Serializer.JSON)

    assert(serde.isInstanceOf[JSONSerde[Item]])
  }

  test("Should recognise Bson") {
    val serde = Serializer.getSerde[Item](Serializer.BSON)

    assert(serde.isInstanceOf[BsonSerde[Item]])
  }

  test("Should recognise Kryo") {
    val serde = Serializer.getSerde[Item](Serializer.KRYO)

    assert(serde.isInstanceOf[KryoSerde[Item]])
  }

  test("Should default to JSON") {
    val serde = Serializer.getSerde[Item]("YAML")

    assert(serde.isInstanceOf[JSONSerde[Item]])
  }
}
