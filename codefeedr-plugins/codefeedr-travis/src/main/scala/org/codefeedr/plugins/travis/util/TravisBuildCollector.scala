package org.codefeedr.plugins.travis.util

import java.time.{LocalDateTime, ZoneId}

import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds}
import org.codefeedr.plugins.travis.util.TravisExceptions._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{blocking, Future}


//TODO only give GithubPushEvent as constructor argument
class TravisBuildCollector(repoOwner: String,
                           repoName: String,
                           branchName: String,
                           pushCommitSha: String,
                           pushDate: LocalDateTime,
                           travis: TravisService,
                           pollingInterval: Int = 30000,
                           timeoutSeconds: Int = 60) {

  private var minimumStartDate: LocalDateTime = pushDate
  private var build: Option[TravisBuild] = None


  /**
    * Keeps requesting the build until it is finished
    * @return A finished Travis build
    */
  def requestFinishedBuild(): Future[TravisBuild] = Future {
    while (!isReady) {

      build = requestBuild
      checkIfBuildShouldBeKnownAlready()

      if (build.nonEmpty){
        print("Found build... Waiting to finish... " + pushCommitSha.substring(0, 4) + " " + build.get.state)
        println(" " + Thread.currentThread().getId + " " + repoName)
      } else {
        println("Not found build yet... Waiting until created... " + repoName)
      }


      if (!isReady) {
        blocking {
          Thread.sleep(pollingInterval)
        }
      }
    }
    build.get
  }

  private def checkIfBuildShouldBeKnownAlready(): Unit = {
    val waitUntil = pushDate.plusSeconds(100 * timeoutSeconds)
    if (build.isEmpty && waitUntil.isBefore(LocalDateTime.now(ZoneId.of("GMT")))) {
      throw BuildNotFoundForTooLongException("Waited " + timeoutSeconds + " seconds for build, but still not found" +
        ", probably because " + repoOwner + "/" + repoName + "is not active on Travis")
    }
  }

  private def isReady: Boolean = {
    if (build.nonEmpty) {
      val state = build.get.state
      return state == "passed" || state == "failed" || state == "canceled" || state == "errored"
    }
    false
  }

  /**
    * Requests the build information based on if a build id is known
    * @return A Travis build
    */
  private def requestBuild: Option[TravisBuild] = {
    try {
      build match {
        case None => requestUnknownBuild()
        case Some(_) => requestKnownBuild()
      }
    } catch {
      case _: CouldNotExtractException =>
        throw CouldNotAccessTravisBuildInfo("Could not retrieve Travis build info for: "
          + repoOwner + "/" + repoName)
      case _: CouldNotGetResourceException =>
        None
      case e: Throwable =>
        e.printStackTrace()
        throw e
    }
  }

  /**
    * Requests a build from travis of which the build id is known
    * @return A TravisBuild if it is found, None otherwise
    */
  private def requestKnownBuild(): Option[TravisBuild] = {
    assert(build.nonEmpty)
    Some(travis.getBuild(build.get.id))
  }

  /**
    * Looks through the Travis builds of the push event to find the build with the corresponding commit sha.
    * The builds are sorted on date, so it only looks after the push date
    * @return A TravisBuild if it is found, None otherwise
    */
  private def requestUnknownBuild(): Option[TravisBuild] = {
    var newestBuildDate: LocalDateTime = LocalDateTime.MIN
    var builds: TravisBuilds = null

    do {
      val offset = if (builds == null) 0 else builds.`@pagination`.next.offset
      builds = travis.getTravisBuilds(repoOwner, repoName, branchName, offset, limit = 5)

      val buildIterator = builds.builds.iterator

      while (buildIterator.hasNext) {
        val x = buildIterator.next()

        // Remember the time at which the latest build has started
        if (x.started_at.isDefined && x.started_at.get.isAfter(newestBuildDate)) {
          newestBuildDate = x.started_at.get
        }

        // If a build has started before the earliest possible that for the target build then stop looking for it
        // and update the the minimum start date
        if (x.started_at.getOrElse(LocalDateTime.MAX).isBefore(minimumStartDate)) {
          minimumStartDate = newestBuildDate
          return None
        }

        // If the build is found return it
        else if (x.commit.sha == pushCommitSha) {
          return Some(x)
        }
      }
    } while (!builds.`@pagination`.is_last)

    None
  }

}