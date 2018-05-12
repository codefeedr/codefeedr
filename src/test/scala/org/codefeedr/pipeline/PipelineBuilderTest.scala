package org.codefeedr.pipeline

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.pipeline.buffer.BufferType
import org.apache.flink.api.scala._
import org.codefeedr.testUtils.{SimpleSinkPipelineObject, SimpleSourcePipelineObject, SimpleTransformPipelineObject, StringType}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class PipelineBuilderTest extends FunSuite with BeforeAndAfter with Matchers {

  var builder: PipelineBuilder = _

  before {
    builder = new PipelineBuilder()
  }

  test("An Simple pipeline configuration throws") {
    assertThrows[EmptyPipelineException] {
      val pipeline = builder.build()
    }
  }

  test("Set buffertype should also be retrievable") {
    builder.setBufferType(BufferType.Kafka)

    assert(builder.getBufferType == BufferType.Kafka)
  }

  test("Every pipeline object should appear in the pipeline (1)") {
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .build()

    assert(pipeline.graph.nodes.size == 1)

    pipeline.graph.nodes.head shouldBe an[SimpleSourcePipelineObject]
  }

  test("Every pipeline object should appear in the pipeline (2)") {
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .append(new SimpleTransformPipelineObject())
      .build()

    assert(pipeline.graph.nodes.size == 2)

    pipeline.graph.nodes.head shouldBe an[SimpleSourcePipelineObject]
    pipeline.graph.nodes.last shouldBe an[SimpleTransformPipelineObject]
  }

  test("Set properties should be available in pipeline properties") {
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .setProperty("key", "value")
      .build()

    assert(pipeline.properties.get("key").get == "value")
  }

  test("Set buffer properties should be available in pipeline buffer properties") {
    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .setBufferProperty("key", "value")
      .build()

    assert(pipeline.bufferProperties.get("key").get == "value")
    assert(pipeline.properties.get("key").isEmpty)
  }

  test("A set keymanager should be forwarded to the pipeline") {
    val km = new StaticKeyManager()

    val pipeline = builder
      .append(new SimpleSourcePipelineObject())
      .setKeyManager(km)
      .build()

    assert(pipeline.keyManager == km)
  }

  test("Building with edges creates a DAG") {

  }

  test("A DAG pipeline can't be appeneded to") {
    builder.edge(new SimpleSourcePipelineObject(), new SimpleTransformPipelineObject())

    assertThrows[IllegalStateException] {
      builder.append(new SimpleSourcePipelineObject())
    }
  }

  test("A sequential pipeline cannot switch to a DAG automatically") {
    builder.append(new SimpleSourcePipelineObject())

    assertThrows[IllegalStateException] {
      builder.edge(new SimpleTransformPipelineObject(), new SimpleSinkPipelineObject())
    }
  }

  test("A sequential pipeline can switch to a DAG manually") {
    builder.append(new SimpleSourcePipelineObject())
    builder.setPipelineType(PipelineType.DAG)

    builder.edge(new SimpleTransformPipelineObject(), new SimpleSinkPipelineObject())
  }

  test("A non-sequential pipeline cannot switch to a sequential pipeline") {
    val a = new SimpleSourcePipelineObject()
    val b = new SimpleTransformPipelineObject()
    val c = new SimpleTransformPipelineObject()

    builder.edge(a, b)
    builder.edge(a, c)

    assert(builder.getPipelineType == PipelineType.DAG)

    assertThrows[IllegalStateException] {
      builder.setPipelineType(PipelineType.Sequential)
    }
  }

  test("Can't add edges to the DAG pipeline twice") {
    val a = new SimpleSourcePipelineObject()
    val b = new SimpleTransformPipelineObject()

    builder.edge(a, b)

    assertThrows[IllegalArgumentException] {
      builder.extraEdge(a, b)
    }
  }

  test("Appending after switching to seq") {
    val a = new SimpleSourcePipelineObject()
    val b = new SimpleTransformPipelineObject()
    val c = new SimpleTransformPipelineObject()

    builder.edge(a, b)
    builder.setPipelineType(PipelineType.Sequential)
    builder.append(c)

    assert(builder.graph.isSequential)
  }

  test("Cannot append same object twice") {
    val a = new SimpleSourcePipelineObject()

    builder.append(a)

    assertThrows[IllegalArgumentException] {
      builder.append(a)
    }
  }

  test("Should disallow extra edge when no main edge is in the graph") {
    val a = new SimpleSourcePipelineObject()
    val b = new SimpleTransformPipelineObject()
    val c = new SimpleTransformPipelineObject()

    assertThrows[IllegalArgumentException] {
      builder.extraEdge(a, b)
    }
  }

  test("Should disallow a second main edge") {
    val a = new SimpleSourcePipelineObject()
    val b = new SimpleTransformPipelineObject()
    val c = new SimpleTransformPipelineObject()

    builder.edge(a, b)

    assertThrows[IllegalArgumentException] {
      builder.edge(c, b)
    }
  }

  test("Append an anonymous pipeline item") {
    val pipeline = builder.append(new SimpleSourcePipelineObject())
      .append { x : DataStream[StringType] =>
        x.map(x => x)
      }.build()

    assert(pipeline.graph.nodes.size == 2)

    pipeline.graph.nodes.head shouldBe an[SimpleSourcePipelineObject]
    pipeline.graph.nodes.last shouldBe an[PipelineObject[StringType, StringType]]
  }

  test("Append an anonymous pipeline job") {
    val pipeline = builder.append(new SimpleSourcePipelineObject())
      .append { x : DataStream[StringType] =>
        x.addSink(new SinkFunction[StringType] {})
      }.build()

    assert(pipeline.graph.nodes.size == 2)

    pipeline.graph.nodes.head shouldBe an[SimpleSourcePipelineObject]
    pipeline.graph.nodes.last shouldBe an[Job[StringType]]
  }
}
