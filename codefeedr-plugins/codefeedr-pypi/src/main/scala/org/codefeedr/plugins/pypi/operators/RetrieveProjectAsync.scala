package org.codefeedr.plugins.pypi.operators

import org.apache.flink.streaming.api.functions.async.{
  ResultFuture,
  RichAsyncFunction
}
import org.codefeedr.plugins.pypi.protocol.Protocol.{
  PyPiProject,
  PyPiRelease,
  PyPiReleaseExt
}
import org.codefeedr.plugins.pypi.util.PyPiService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import collection.JavaConverters._

class RetrieveProjectAsync
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
