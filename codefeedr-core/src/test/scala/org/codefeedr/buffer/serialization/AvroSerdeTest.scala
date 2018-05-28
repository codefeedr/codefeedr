package org.codefeedr.buffer.serialization

import java.util.Date

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.codefeedr.pipeline.PipelineItem
import org.scalatest.{BeforeAndAfter, FunSuite}

case class SimpleCaseClass(str: String, i: Int)

case class ComplexSuperCaseClass(list : List[String], complex : Option[ComplexCaseClass])
case class ComplexCaseClass(date : Option[Date])

class AvroSerdeTest extends FunSuite with BeforeAndAfter {

  private var serdeSimple : AvroSerde[SimpleCaseClass] = null

  before {
    serdeSimple = AvroSerde[SimpleCaseClass]
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

  test ("AvroSerde complex case class check") {
    val serde = AvroSerde[ComplexSuperCaseClass]

    val complexCase = ComplexCaseClass(Some(new Date))
    val complexSuperClass = ComplexSuperCaseClass(List("123"), Some(complexCase))

    val serialized = serde.serialize(complexSuperClass)
    val deserialized = serde.deserialize(serialized)

    assert(complexSuperClass == deserialized)
  }

}

