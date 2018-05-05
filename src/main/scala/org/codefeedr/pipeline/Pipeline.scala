package org.codefeedr.pipeline

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.codefeedr.ImmutableProperties
import org.codefeedr.keymanager.KeyManager
import org.codefeedr.pipeline.buffer.BufferType.BufferType
import org.codefeedr.pipeline.Runtime.Runtime

case class Pipeline(bufferType: BufferType,
                    bufferProperties: ImmutableProperties,
                    objects: Seq[PipelineObject[PipelinedItem, PipelinedItem]],
                    properties: ImmutableProperties,
                    keyManager: KeyManager) {
  val environment: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

  def start(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    var runtime = Runtime.Local

    // If a pipeline object is specified, set to cluster
    val stage = params.get("stage")
    if (stage != null) {
      runtime = Runtime.Cluster
    }

    // Override runtime with runtime parameter
    runtime = params.get("runtime") match {
      case "mock" => Runtime.Mock
      case "local" => Runtime.Local
      case "cluster" => Runtime.Cluster
      case _ => runtime
    }

    start(runtime, stage)
  }

  def start(runtime: Runtime, stage: String = null): Unit = {
    runtime match {
      case Runtime.Mock => startMock()
      case Runtime.Local => startLocal()
      case Runtime.Cluster => startClustered(stage)
    }
  }

  // Without any buffers. Connect all POs to each other
  def startMock(): Unit = {
    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // Connect each object by getting a starting buffer, if any, and sending it to the next.
    var buffer: DataStream[PipelinedItem] = null
    for (obj <- objects) {
      buffer = obj.transform(buffer)
    }

    environment.execute("CodeFeedr Mock Job")
  }

  // With buffers, all in same program
  def startLocal(): Unit = {
    // Run all setups
    for (obj <- objects) {
      obj.setUp(this)
    }

    // For each PO, make buffers and run
    for (obj <- objects) {
      runObject(obj)
    }

    environment.execute("CodeFeedr Local Job")
  }

  // With buffers, running just one PO
  def startClustered(stage: String): Unit = {
    // TODO: find that PO
    val obj = objects.head
    obj.setUp(this)
    runObject(obj)

    environment.execute("CodeFeedr Cluster Job")
  }

  /**
    * Run a pipeline object.
    *
    * Creates a source and sink for the object and then runs the transform function.
    * @param obj
    */
  private def runObject(obj: PipelineObject[PipelinedItem, PipelinedItem]): Unit = {
    // TODO: get main source, based on graph
    lazy val source = if (obj.hasMainSource) obj.getMainSource else null

    lazy val sink = if (obj.hasSink) obj.getSink else null

    val transformed = obj.transform(source)

    if (obj.hasSink) {
      transformed.addSink(sink)
    }
  }

}