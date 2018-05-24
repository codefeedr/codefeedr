package org.codefeedr.buffer.serialization

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.scalatest.{BeforeAndAfter, FunSuite}

case class SimpleCaseClass(str: String, i: Int)

class AvroSerdeTest extends FunSuite with BeforeAndAfter {

  private var serdeSimple : AvroSerde[SimpleCaseClass] = null

  before {
    serdeSimple = new AvroSerde[SimpleCaseClass]()
  }

  test ("AvroSerde simple serialized values") {
    val simple = new SimpleCaseClass("test", 20)

    val serialized = serdeSimple.serialize(simple)
    val deserialized = serdeSimple.deserialize(serialized)

    assert(simple == deserialized)
  }

  test ("AvroSerde simple serialized values negative") {
    val simple = new SimpleCaseClass("test", 20)
    val simpleDiff = new SimpleCaseClass("testNegative", 20)

    val serialized = serdeSimple.serialize(simple)
    val deserialized = serdeSimple.deserialize(serialized)

    assert(simpleDiff != deserialized)
  }

  test ("AvroSerde TypeInformation check") {
    val typeInfo = TypeInformation.of(classOf[SimpleCaseClass])

    assert(typeInfo == serdeSimple.getProducedType)
  }

}

