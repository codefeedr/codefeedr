package org.codefeedr.pipeline

trait PipelineObject[In, Out] {

  def setup() {}

  def main(pipeline: Pipeline)

  //  def getStorageSource[T](typ: String, collection: String): DataStream[T] = {
  //
  //  }
  //
  //  def getStorageSink[T](typ: String, collection: String): DataSink[T] = {
  //
  //  }
  //
  //  def getSource(): DataStream[In] = {
  //
  //  }
  //
  //  def getSink(): DataSink[Out] = {
  //
  //  }
}
