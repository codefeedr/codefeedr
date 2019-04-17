package org.codefeedr.plugins.pypi.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.accumulators.LongCounter
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.datastream.{
  AsyncDataStream => JavaAsyncDataStream
}
import org.apache.flink.streaming.api.functions.async.{
  AsyncFunction => JavaAsyncFunction
}
import org.codefeedr.plugins.pypi.protocol.Protocol.{
  PyPiProject,
  PyPiRelease,
  PyPiReleaseExt
}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.async.{
  ResultFuture,
  RichAsyncFunction
}
import org.codefeedr.plugins.pypi.util.PyPiService

import collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiReleaseExt](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiReleaseExt] = {

    val async = JavaAsyncDataStream.orderedWait(source.javaStream,
                                                new MapReleaseToProject,
                                                5,
                                                TimeUnit.SECONDS,
                                                100)

    new org.apache.flink.streaming.api.scala.DataStream(async)
      .map(x => (x.project.info.name, x.project.releases))
      .print()

    new org.apache.flink.streaming.api.scala.DataStream(async)
  }
}

/** Maps a [[PyPiRelease]] to a [[PyPiProject]]. */
class MapReleaseToProject
    extends RichAsyncFunction[PyPiRelease, PyPiReleaseExt] {

  implicit lazy val executor: ExecutionContext = ExecutionContext.global

  override def asyncInvoke(input: PyPiRelease,
                           resultFuture: ResultFuture[PyPiReleaseExt]): Unit = {
    val projectName = input.title.replace(" ", "/")
    val requestProject: Future[Option[PyPiProject]] = Future(
      PyPiService.getProject(projectName))

    requestProject.onComplete {
      case Success(result: Option[PyPiProject]) => {
        if (result.isDefined) {
          resultFuture.complete(
            List(
              PyPiReleaseExt(input.title,
                             input.link,
                             input.description,
                             input.pubDate,
                             result.get)).asJava)
        } else {
          resultFuture.complete(List().asJava)
        }
      }
      case Failure(e) =>
        resultFuture.complete(List().asJava)
        e.printStackTrace()
    }

  }

  override def timeout(input: PyPiRelease,
                       resultFuture: ResultFuture[PyPiReleaseExt]): Unit = {
    resultFuture.complete(List().asJava)
  }
}
