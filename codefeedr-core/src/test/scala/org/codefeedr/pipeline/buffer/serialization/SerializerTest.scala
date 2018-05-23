package org.codefeedr.pipeline.buffer.serialization

import org.codefeedr.pipeline.PipelineItem
import org.scalatest.FunSuite

class SerializerTest extends FunSuite {

  case class Item() extends PipelineItem

  test("Should recognise AVRO") {
    val serde = Serializer.getSerde[Item](Serializer.AVRO)

    assert(serde.isInstanceOf[AvroSerde[Item]])
  }

  test("Should recognise JSON") {
    val serde = Serializer.getSerde[Item](Serializer.JSON)

    assert(serde.isInstanceOf[JSONSerde[Item]])
  }

  test("Should default to JSON") {
    val serde = Serializer.getSerde[Item]("YAML")

    assert(serde.isInstanceOf[JSONSerde[Item]])
  }
}
