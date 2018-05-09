/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.pipeline

import com.sksamuel.avro4s.FromRecord
import org.apache.flink.api.java.operators.DataSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.buffer.BufferFactory

import scala.reflect.{ClassTag, Manifest}
import scala.reflect.runtime.universe._

/**
  * This class represents a processing job within the pipeline.
  *
  * @tparam In  input type for this pipeline object.
  * @tparam Out output type for this pipeline object.
  */
abstract class PipelineObject[In <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] {

  var pipeline: Pipeline = _

  /**
    * Setups the pipeline object with a pipeline.
    * @param pipeline the pipeline it belongs to.
    */
  def setUp(pipeline: Pipeline): Unit = {
    this.pipeline = pipeline
  }

  /**
    * Transforms the pipeline object from its input type to its output type.
    * This requires using the Flink DataStream API.
    *
    * @param source the input source.
    * @return the transformed stream.
    */
  def transform(source: DataStream[In]): DataStream[Out]

  /**
    * Removes the pipeline.
    */
  def tearDown(): Unit = {
    this.pipeline = null
  }

  /**
    * Verify that the object is valid.
    *
    * Checks types of the input sources and whether the graph is configured correctly for the types.
    */
  protected[pipeline] def verifyGraph(): Unit = {}

  /**
    * Get all parents for this object
    *
    * @return set of parents. Can be empty
    */
  def getParents: Vector[PipelineObject[PipelineItem, PipelineItem]] =
    pipeline.graph.getParents(this).asInstanceOf[Vector[PipelineObject[PipelineItem, PipelineItem]]]

  /**
    * Check if this pipeline object is sourced from a Buffer.
    * @return if this object has a (buffer) source.
    */
  def hasMainSource: Boolean =
    typeOf[In] != typeOf[NoType] && pipeline.graph.getMainParent(this).isDefined

  /**
    * Check if this pipeline object is sinked to a Buffer.
    * @return if this object has a (buffer) sink.
    */
  def hasSink: Boolean = typeOf[Out] != typeOf[NoType]

  /**
    * Returns the buffer source of this pipeline object.
    * @return the DataStream resulting from the buffer.
    */
  def getMainSource: DataStream[In] = {
    assert(pipeline != null)

    if (!hasMainSource) {
      throw NoSourceException("PipelineObject defined NoType as In type. Buffer can't be created.")
    }



    val parentNode = pipeline.graph.getMainParent(this).get.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]]

    println("Get source with subject", getSinkSubject, parentNode.getSinkSubject)

    val factory = new BufferFactory(pipeline, parentNode)
    val buffer = factory.create[In]()

    buffer.getSource
  }

  /**
    * Returns the buffer sink of this pipeline object.
    * @return the SinkFunction resulting from the buffer.
    */
  def getSink: SinkFunction[Out] = {
    assert(pipeline != null)

    if (!hasSink) {
      throw NoSinkException("PipelineObject defined NoType as Out type. Buffer can't be created.")
    }

    val factory = new BufferFactory(pipeline, this)
    val buffer = factory.create[Out]()

    buffer.getSink
  }

  /**
    * Get the sink subject used by the buffer.
    *
    * This is also used for child objects to read from the buffer again.
    *
    * @return Sink subject
    */
  def getSinkSubject: String = this.getClass.getName

  def getSource[T <: AnyRef : Manifest : FromRecord](parentNode: PipelineObject[PipelineItem, PipelineItem]): DataStream[T] = {
    assert(parentNode != null)

    val factory = new BufferFactory(pipeline, parentNode)
    val buffer = factory.create[T]()

    buffer.getSource
  }

  def getStorageSource[T](collection: String): DataStream[T] = ???

  def getStorageSink[T](collection: String): DataSink[T] = ???

}

abstract class PipelineObject2[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, Out] {

  override def transform(source: DataStream[In]): DataStream[Out] =
    transform(source, getSource[In2](getParents(1)))

  override def verifyGraph(): Unit = {
    if (typeOf[In] == typeOf[NoType] || typeOf[In2] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (getParents.size < 2) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2]): DataStream[Out]

}

abstract class PipelineObject3[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, In3 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, Out] {

  override def transform(source: DataStream[In]): DataStream[Out] =
    transform(source, getSource[In2](getParents(1)), getSource[In3](getParents(2)))

  override def verifyGraph(): Unit = {
    if (typeOf[In] == typeOf[NoType] || typeOf[In2] == typeOf[NoType] || typeOf[In3] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (getParents.size < 3) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2], thirdSource: DataStream[In3]): DataStream[Out]

}

abstract class PipelineObject4[In <: PipelineItem : ClassTag : Manifest : FromRecord, In2 <: PipelineItem : ClassTag : Manifest : FromRecord, In3 <: PipelineItem : ClassTag : Manifest : FromRecord, In4 <: PipelineItem : ClassTag : Manifest : FromRecord, Out <: PipelineItem : ClassTag : Manifest : FromRecord] extends PipelineObject[In, Out] {

  override def transform(source: DataStream[In]): DataStream[Out] =
    transform(source, getSource[In2](getParents(1)), getSource[In3](getParents(2)), getSource[In4](getParents(3)))

  override def verifyGraph(): Unit = {
    if (typeOf[In] == typeOf[NoType] || typeOf[In2] == typeOf[NoType] || typeOf[In3] == typeOf[NoType] || typeOf[In4] == typeOf[NoType]) {
      throw new IllegalStateException("Cannot use NoType on pipeline objects with multiple input sources")
    }

    if (getParents.size < 4) {
      throw new IllegalStateException(s"Parents of multi-source object should all be configured in pipeline graph. Missing parents for ${getClass.getName}")
    }
  }

  def transform(source: DataStream[In], secondSource: DataStream[In2], thirdSource: DataStream[In3], fourthSource: DataStream[In4]): DataStream[Out]

}
