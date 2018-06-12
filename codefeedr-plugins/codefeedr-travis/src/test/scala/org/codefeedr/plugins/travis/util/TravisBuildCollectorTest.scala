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
 */
package org.codefeedr.plugins.travis.util

import java.util.Date

import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.plugins.travis.util.TravisExceptions.{BuildNotFoundForTooLongException, CouldNotAccessTravisBuildInfo, CouldNotGetResourceException}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.FunSuite

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class TravisBuildCollectorTest extends FunSuite{

  test("Request Finished build should return a finished build") {

    val travis = spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))

    val firstReturn = Source.fromInputStream(getClass.getResourceAsStream("/codefeedr_builds")).getLines().next()
    val secondReturn = Source.fromInputStream(getClass.getResourceAsStream("/codefeedr_build")).getLines().next()
    doReturn(firstReturn).doReturn(secondReturn).when(travis).getTravisResource(any(classOf[String]))

    val collector = new TravisBuildCollector(
      "owner",
      "name",
      "branch",
      "42e494344aae63b0994bc26d05ac2f68e84cb40e",
      new Date(0L),
      travis,
      0)

    val future = collector.requestFinishedBuild()
    val result = Await.result(future, Duration.Inf)

    assert(result.state == "passed")
  }

  test("Should stop if the build hasn't been created for too long") {

    val travis = spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))

    val firstReturn = Source.fromInputStream(getClass.getResourceAsStream("/codefeedr_builds")).getLines().next()
    doReturn(firstReturn).when(travis).getTravisResource(any(classOf[String]))

    val collector = new TravisBuildCollector(
      "owner",
      "name",
      "branch",
      "nonExistingSha",
      new Date(0L),
      travis,
      0)

    val future = collector.requestFinishedBuild()
    assertThrows[BuildNotFoundForTooLongException] {
      Await.result(future, Duration.Inf)
    }
  }

  test("RequestBuild should throw exception if the repo of the build is not available") {
    val travis = spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val firstReturn = Source.fromInputStream(getClass.getResourceAsStream("/repo_not_found")).getLines().next()
    doReturn(firstReturn).when(travis).getTravisResource(any(classOf[String]))

    val collector = new TravisBuildCollector(
      "owner",
      "name",
      "branch",
      "sha",
      new Date(0L),
      travis,
      0)

    assertThrows[CouldNotAccessTravisBuildInfo] {
      collector.requestBuild
    }
  }

  test("RequestBuild should return None if the request isn't answered") {
    val travis = spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    doThrow(CouldNotGetResourceException()).when(travis).getTravisResource(any(classOf[String]))

    val collector = new TravisBuildCollector(
      "owner",
      "name",
      "branch",
      "sha",
      new Date(0L),
      travis,
      0)

    assert(collector.requestBuild.isEmpty)
  }

}
