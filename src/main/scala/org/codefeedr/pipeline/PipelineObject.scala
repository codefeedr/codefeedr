package org.codefeedr.pipeline

abstract class PipelineObject[+In <: PipelinedItem, +Out <: PipelinedItem] {

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
//    def getSource(): DataStream[In] = {
//      StreamExecutionEnvironment.getExecutionEnvironment
//        .readTextFile("test")
//    }

//    def getSink(): DataSink[Out] = {
//
//    }

  // TODO: Add pipeline building with ->
}
