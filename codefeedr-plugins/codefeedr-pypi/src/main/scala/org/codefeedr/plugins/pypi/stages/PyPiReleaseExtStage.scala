package org.codefeedr.plugins.pypi.stages

import java.util.concurrent.TimeUnit

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream}
import org.codefeedr.plugins.pypi.protocol.Protocol.{
  PyPiProject,
  PyPiRelease,
  PyPiReleaseExt
}
import org.codefeedr.stages.TransformStage
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.codefeedr.plugins.pypi.util.PyPiService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
class PyPiReleaseExtStage(stageId: String = "pypi_releases")
    extends TransformStage[PyPiRelease, PyPiReleaseExt](Some(stageId)) {
  override def transform(
      source: DataStream[PyPiRelease]): DataStream[PyPiReleaseExt] = {

    val async = AsyncDataStream.orderedWait(source,
                                            new MapReleaseToProject,
                                            5,
                                            TimeUnit.SECONDS,
                                            100)

    async.map(x => (x.project.info.name, x.project.releases)).print()

    async
  }
}

/** Maps a [[PyPiRelease]] to a [[PyPiProject]]. */
class MapReleaseToProject extends AsyncFunction[PyPiRelease, PyPiReleaseExt] {

  implicit lazy val executor: ExecutionContext = ExecutionContext.global

  override def asyncInvoke(input: PyPiRelease,
                           resultFuture: ResultFuture[PyPiReleaseExt]): Unit = {
    val projectName = input.title.split(" ")(0)

    val requestProject: Future[Option[PyPiProject]] = Future(
      PyPiService.getProject(projectName))

    requestProject.onComplete {
      case Success(result: Option[PyPiProject]) => {
        if (result.isDefined)
          resultFuture.complete(
            Iterable(
              PyPiReleaseExt(input.title,
                             input.link,
                             input.description,
                             input.pubDate,
                             result.get)))
      }
      case Failure(e) =>
        resultFuture.complete(Iterable())
        e.printStackTrace()
    }

  }

  override def timeout(input: PyPiRelease,
                       resultFuture: ResultFuture[PyPiReleaseExt]): Unit = {
    println("HEREEE")
  }
}
