package org.codefeedr.pipeline

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

class Pipeline {

  def start(args: Array[String]) = {

  }

  def getEnvironment : StreamExecutionEnvironment = {
    StreamExecutionEnvironment.getExecutionEnvironment
  }
}