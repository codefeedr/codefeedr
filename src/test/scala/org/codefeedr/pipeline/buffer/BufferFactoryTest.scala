package org.codefeedr.pipeline.buffer

import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.testUtils.{EmptySourcePipelineObject, EmptyTransformPipelineObject, StringType}
import org.scalatest.{BeforeAndAfter, FunSuite}

class BufferFactoryTest extends FunSuite with BeforeAndAfter {

  val nodeA = new EmptySourcePipelineObject()
  val nodeB = new EmptyTransformPipelineObject()

  test("Should throw when creating a buffer with no buffertype") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.None)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeB)

    assertThrows[IllegalStateException] {
      factory.create[StringType]()
    }
  }

  test("Should throw when giving a null object") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.None)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, null)

    assertThrows[IllegalArgumentException] {
      factory.create[StringType]()
    }
  }

  test("Should give a configured Kafka buffer when buffertype is kafka") {
    val pipeline = new PipelineBuilder()
      .setBufferType(BufferType.Kafka)
      .append(nodeA)
      .append(nodeB)
      .build()

    val factory = new BufferFactory(pipeline, nodeB)


    // created for nodeB sink, so should have subject of nodeB
    val nodeSubject = nodeB.getSinkSubject
    val buffer = factory.create[StringType]()

    assert(buffer.isInstanceOf[KafkaBuffer[StringType]])
  }

}
