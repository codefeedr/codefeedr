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

/** Retrieves a project related to a release asynchronously. */
class RetrieveProjectAsync
    extends RichAsyncFunction[PyPiRelease, PyPiReleaseExt] {

  /** Retrieve the execution context lazily. */
  implicit lazy val executor: ExecutionContext = ExecutionContext.global

  /** Async retrieves the project belonging to the release.
    *
    * @param input the release.
    * @param resultFuture the future to add the project to.
    */
  override def asyncInvoke(input: PyPiRelease,
                           resultFuture: ResultFuture[PyPiReleaseExt]): Unit = {

    /** The project name combined with its release version */
    val projectName = input.title.replace(" ", "/")

    /** Retrieve the project in a Future. */
    val requestProject: Future[Option[PyPiProject]] = Future(
      PyPiService.getProject(projectName))

    /** Collects the result. */
    requestProject.onComplete {
      case Success(result: Option[PyPiProject]) => {
        if (result.isDefined) { //If we get None, we return nothing.
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

  /** If we retrieve a time-out, then we just complete the future with an empty list.*/
  override def timeout(input: PyPiRelease,
                       resultFuture: ResultFuture[PyPiReleaseExt]): Unit =
    resultFuture.complete(List().asJava)
}
