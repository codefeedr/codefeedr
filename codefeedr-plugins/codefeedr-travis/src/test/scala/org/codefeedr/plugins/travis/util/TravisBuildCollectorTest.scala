package org.codefeedr.plugins.travis.util

import java.time.LocalDateTime

import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.plugins.travis.TravisProtocol.TravisBuild
import org.codefeedr.plugins.travis.util.TravisExceptions.{BuildNotFoundForTooLongException, CouldNotAccessTravisBuildInfo, CouldNotExtractException, CouldNotGetResourceException}
import org.mockito.Matchers.any
import org.scalatest.FunSuite
import org.mockito.Mockito._

import scala.concurrent.Await
import scala.io.Source
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

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
      LocalDateTime.MIN,
      travis,
      0)

    val future = collector.requestFinishedBuild()
    val result = Await.result(future, Duration.Inf)

    assert(result.state == "passed")
  }

  test("Should stop if the build hasn't been created for too long") {

    val travis = spy(new TravisService(new StaticKeyManager(Map("travis" -> "dummy_key"))))

    val firstReturn = Source.fromInputStream(getClass.getResourceAsStream("/codefeedr_builds")).getLines().next()
//    val secondReturn = Source.fromInputStream(getClass.getResourceAsStream("/codefeedr_build")).getLines().next()
    doReturn(firstReturn).when(travis).getTravisResource(any(classOf[String]))

    val collector = new TravisBuildCollector(
      "owner",
      "name",
      "branch",
      "nonExistingSha",
      LocalDateTime.MIN,
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
      LocalDateTime.MIN,
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
      LocalDateTime.MIN,
      travis,
      0)

    assert(collector.requestBuild.isEmpty)
  }

}
