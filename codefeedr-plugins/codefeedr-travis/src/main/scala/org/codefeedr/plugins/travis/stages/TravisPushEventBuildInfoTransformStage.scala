package org.codefeedr.plugins.travis.stages

import java.util.concurrent.{Executors, TimeUnit}

import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, _}
import org.codefeedr.pipeline.{Pipeline, TransformStage}
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.travis.TravisProtocol.{PushEventFromActiveTravisRepo, TravisBuild}
import org.codefeedr.plugins.travis.util.{TravisBuildCollector, TravisService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class TravisPushEventBuildInfoTransformStage(capacity: Int = 100) extends TransformStage[PushEventFromActiveTravisRepo, TravisBuild]{

  var travis: TravisService = _

  override def setUp(pipeline: Pipeline): Unit = {
    super.setUp(pipeline)
    travis = new TravisService(pipeline.keyManager)
  }

  override def transform(source: DataStream[PushEventFromActiveTravisRepo]): DataStream[TravisBuild] = {

    AsyncDataStream.unorderedWait(
      source.map(x => x.pushEventItem),
      new TravisBuildStatusRequest(travis),
      20,
      TimeUnit.MINUTES,
      capacity)
  }
}


private class TravisBuildStatusRequest(travis: TravisService) extends AsyncFunction[PushEvent, TravisBuild] {

  var futures: List[String] = List()

  override def asyncInvoke(input: PushEvent, resultFuture: ResultFuture[TravisBuild]): Unit = {
    // If there are no commits in the push then there will be no build
    if (input.payload.commits.isEmpty) return

    val splittedSlug =  input.repo.name.split('/')
    val repoOwner = splittedSlug(0)
    val repoName = splittedSlug(1)
    val branchName = input.payload.ref.replace("refs/heads/", "")
    val commitSHA = input.payload.head
    val pushDate = input.created_at

//    implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    val futureResultBuild: Future[TravisBuild] =
      new TravisBuildCollector(repoOwner, repoName, branchName, commitSHA, pushDate, travis).requestFinishedBuild()

    futureResultBuild.onComplete {
      case Success(result: TravisBuild) => resultFuture.complete(Iterable(result))
      case Failure(e) => e.printStackTrace()
    }

    futures :+= repoName
    println(futures.size, futures)
  }
}
