package org.codefeedr.plugins.travis.stages

import org.apache.flink.streaming.api.scala.async.ResultFuture
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.plugins.github.GitHubProtocol.PushEvent
import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds}
import org.codefeedr.plugins.travis.util.TravisService
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTimeSerializers
import org.scalatest.FunSuite
import org.json4s.jackson.JsonMethods.parse
import org.mockito.Matchers.any
import org.scalamock.scalatest.MockFactory
import org.mockito.Mockito._

import scala.io.Source

class TravisPushEventBuildInfoTransformStageTest extends FunSuite with MockFactory {

  test("Travis build status request gives a travis build") {
    implicit val formats: Formats = DefaultFormats ++ JavaTimeSerializers.all
    val pushEvent =
      parse(Source.fromInputStream(getClass.getResourceAsStream("/single_push_event.json")).mkString)
      .extract[PushEvent]

    val travisBuilds =
      parse(Source.fromInputStream(getClass.getResourceAsStream("/single_push_event_build.json")).mkString)
      .extract[TravisBuilds]

    val travis = spy(new TravisService(new StaticKeyManager))
    doReturn(travisBuilds)
      .when(travis)
      .getTravisBuilds(any(classOf[String]), any(classOf[String]), any(classOf[String]), any(classOf[Int]), any(classOf[Int]))

    val travisBuildStatusRequest = new TravisBuildStatusRequest(travis)

    var iterable: Iterable[TravisBuild] = null

    val resultFuture: ResultFuture[TravisBuild] = new ResultFuture[TravisBuild] {
      override def complete(result: Iterable[TravisBuild]): Unit = {
        iterable = result
      }

      override def completeExceptionally(throwable: Throwable): Unit = {fail()}
    }
    travisBuildStatusRequest.asyncInvoke(pushEvent, resultFuture)

    while(iterable == null) {
      Thread.sleep(100)
    }

    assert(iterable.head.state == "passed")
  }

}