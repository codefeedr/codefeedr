/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.plugins.travis.util

import java.time.{LocalDateTime, ZoneId}
import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds}
import org.codefeedr.plugins.travis.util.TravisExceptions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{blocking, Future}

/**
  * Class that keeps querying Travis about a certain push event until it is found.
  * @param repoOwner Owner of the repo of the push event
  * @param repoName Name of the repo of the push event
  * @param branchName Name of the branch of the push event
  * @param pushCommitSha Sha of the commit of the push event
  * @param pushDate Date of the push event
  * @param travis TravisService used to query Travis
  * @param pollingInterval Interval at which builds are queried
  * @param timeoutSeconds How long it will wait for the build to be created
  */
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

      if (!isReady) {
        blocking {
          Thread.sleep(pollingInterval)
        }
      }
    }
    build.get
  }

  def checkIfBuildShouldBeKnownAlready(): Unit = {
    val waitUntil = pushDate.plusSeconds(100 * timeoutSeconds)
    if (build.isEmpty && waitUntil.isBefore(LocalDateTime.now(ZoneId.of("GMT")))) {
      throw BuildNotFoundForTooLongException("Waited " + timeoutSeconds + " seconds for build, but still not found" +
        ", probably because " + repoOwner + "/" + repoName + "is not active on Travis")
    }
  }

  /**
    * Checks whether or not a build is completed.
    * @return Whether or not a build is completed
    */
  def isReady: Boolean = {
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
  def requestBuild: Option[TravisBuild] = {
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
    }
  }

  /**
    * Requests a build from travis of which the build id is known
    * @return A TravisBuild if it is found, None otherwise
    */
  def requestKnownBuild(): Option[TravisBuild] = {
    assert(build.nonEmpty)
    Some(travis.getBuild(build.get.id))
  }

  /**
    * Looks through the Travis builds of the push event to find the build with the corresponding commit sha.
    * The builds are sorted on date, so it only looks after the push date
    * @return A TravisBuild if it is found, None otherwise
    */
  def requestUnknownBuild(): Option[TravisBuild] = {
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