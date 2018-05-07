package org.codefeedr.pipeline.buffer.serialization

import org.scalatest.{BeforeAndAfter, FunSuite}

case class SimpleCaseClass(str: String, i: Int)

class AvroSerdeTest extends FunSuite with BeforeAndAfter {

  private var serde : AvroSerde[SimpleCaseClass] = null

  before {
    serde = new AvroSerde[SimpleCaseClass]()
  }

  test ("AvroSerde simple serialized values") {
    val simple = new SimpleCaseClass("test", 20)

    val serialized = serde.serialize(simple)
    val deserialized = serde.deserialize(serialized)

    assert(simple == deserialized)
  }

  test ("AvroSerde simple serialized values negative") {
    val simple = new SimpleCaseClass("test", 20)
    val simpleDiff = new SimpleCaseClass("testNegative", 20)

    val serialized = serde.serialize(simple)
    val deserialized = serde.deserialize(serialized)

    assert(simpleDiff != deserialized)
  }



}

