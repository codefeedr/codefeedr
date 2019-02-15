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

import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.plugins.travis.util.TravisExceptions.CouldNotExtractException
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.FunSuite

import scala.io.Source

class TravisServiceTest extends FunSuite {

  test("Get Builds should convert http request body to builds object") {

    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/codefeedr_builds"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    val builds =
      travisService.getTravisBuilds("joskuijpers", "bep_codefeedr", "develop")

    assert(builds.builds.size == 5)

  }

  test("Could Not extract error should be thrown when repo cannot be found") {

    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/repo_not_found"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    assertThrows[CouldNotExtractException] {
      travisService.getTravisBuilds("joskuijpers", "bep_codefeed", "develop")
    }
  }

  test("Get builds should throw exception when the return body is not parsable") {

    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/not_json_parsable"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    assertThrows[Exception] {
      travisService.getTravisBuilds("joskuijpers", "bep_codefeed", "develop")
    }
  }

  test("Get build by id should return a single build") {
    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/codefeedr_build"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    val build = travisService.getBuild(380111810)
    assert(build.commit.message == "Added more tests for EventService.")

  }

  test("Travis active repo filter should return true when active is true") {
    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/codefeedr_repo"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    val filter = travisService.repoIsActiveFilter
    assert(filter("joskuijpers%2Fbep_codefeedr"))
  }

  test("Travis active repo filter should return false when repo is not found") {
    val travisService =
      spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))
    val returnBody = Source
      .fromInputStream(getClass.getResourceAsStream("/repo_not_found"))
      .getLines()
      .next()
    doReturn(returnBody)
      .when(travisService)
      .getTravisResource(any(classOf[String]))

    val filter = travisService.repoIsActiveFilter
    assert(!filter("joskuijpers%2Fbep_codefeedr"))
  }

  test("Travis key should be in header") {
    val travis =
      new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key")))

    val headers = travis.getHeaders

    assert(headers(1) == ("Authorization", "token dummy_key"))
  }

}
