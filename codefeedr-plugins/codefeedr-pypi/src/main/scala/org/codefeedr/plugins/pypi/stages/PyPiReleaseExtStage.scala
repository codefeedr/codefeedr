package org.codefeedr.plugins.pypi.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream}
import org.codefeedr.plugins.pypi.protocol.Protocol.{PyPiProject, PyPiRelease}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.codefeedr.plugins.pypi.util.PyPiService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiProject](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiProject] = {

    AsyncDataStream.orderedWait(source,
                                new MapReleaseToProject,
                                5,
                                TimeUnit.SECONDS,
                                100)
  }
}

/** Maps a [[PyPiRelease]] to a [[PyPiProject]]. */
class MapReleaseToProject extends AsyncFunction[PyPiRelease, PyPiProject] {

  implicit val executor: ExecutionContext = ExecutionContext.global

  override def asyncInvoke(input: PyPiRelease,
                           resultFuture: ResultFuture[PyPiProject]): Unit = {
    val projectName = input.title.split(" ")(0)

    val requestProject: Future[Option[PyPiProject]] = Future(
      PyPiService.getProject(projectName))

    requestProject.onComplete {
      case Success(result: Option[PyPiRelease]) => {
        if (result.isDefined) resultFuture.complete(Iterable(result.get))
      }
      case Failure(e) =>
        resultFuture.complete(Iterable())
        e.printStackTrace()
    }

  }

  override def timeout(input: PyPiRelease,
                       resultFuture: ResultFuture[PyPiProject]): Unit = {}
}
