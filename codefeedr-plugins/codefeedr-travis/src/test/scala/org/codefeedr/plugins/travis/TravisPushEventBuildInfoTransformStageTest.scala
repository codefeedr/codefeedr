package org.codefeedr.plugins.travis

import org.apache.flink.streaming.api.scala._
import org.codefeedr.keymanager.StaticKeyManager
import org.codefeedr.pipeline.PipelineBuilder
import org.codefeedr.plugins.github.stages.{GitHubEventToPushEvent, GitHubEventsInput}
import org.codefeedr.plugins.travis.TravisProtocol.TravisBuild
import org.codefeedr.plugins.travis.stages.{TravisFilterActiveReposTransformStage, TravisPushEventBuildInfoTransformStage}
import org.scalatest.FunSuite

import scala.io.Source

object TravisPushEventBuildInfoTransformStageTest {

  def main(args: Array[String]): Unit = {

    new PipelineBuilder()
      .setKeyManager(new StaticKeyManager(Map("travis" -> Source.fromInputStream(getClass.getResourceAsStream("/travis_api_key")).getLines().next(),
        "events_source" -> Source.fromInputStream(getClass.getResourceAsStream("/github_api_key")).getLines().next())))

      .append(new GitHubEventsInput())
      .append(new GitHubEventToPushEvent())
      .append(new TravisFilterActiveReposTransformStage)
      .append(new TravisPushEventBuildInfoTransformStage(4))
      .append{x: DataStream[TravisBuild] =>
        x.map(x => (x.repository.slug, x.state, x.duration.getOrElse(0), x.branch.name, x.commit.sha)).print()
      }
      .build()
//      .startMock()

  }
}

class TravisPushEventBuildInfoTransformStageTest extends FunSuite {



}