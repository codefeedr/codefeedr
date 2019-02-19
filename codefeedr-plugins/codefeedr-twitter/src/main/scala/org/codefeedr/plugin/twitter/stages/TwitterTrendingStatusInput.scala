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
package org.codefeedr.plugin.twitter.stages

import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.danielasfregola.twitter4s.entities._
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import grizzled.slf4j.Logging
import org.apache.flink.api.common.time.Time
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{
  RichSourceFunction,
  SourceFunction
}
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.plugin.twitter.TwitterProtocol.TweetWrapper
import org.codefeedr.stages.InputStage
import org.apache.flink.api.scala._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/**
  * InputStage which retrieves tweets based on current trending topics.
  * Refreshes topics after $sleepTime minutes.
  *
  * @param consumerToken  Consumer Token of your application.
  * @param accessToken  Access Token of your application.
  * @param sleepTime Time in minutes to refresh trending topics and restart stream.
  *                  Trending topics are cached for 5 minutes, so you shouldn't set this lower than that.
  *                  Consider that there is some overhead in stopping and restarting the stream.
  */
class TwitterTrendingStatusInput(consumerToken: ConsumerToken,
                                 accessToken: AccessToken,
                                 sleepTime: Int = 15,
                                 stageId: Option[String] = None)
    extends InputStage[TweetWrapper](stageId) {

  override def main(): DataStream[TweetWrapper] = {
    environment.addSource(
      new TwitterTrendingStatusSource(consumerToken, accessToken, sleepTime))
  }
}

/**
  * Flink source which retrieves tweets based on current trending topics.
  * Refreshes topics after $sleepTime minutes.
  *
  * @param consumerToken  Consumer Token of your application.
  * @param accessToken  Access Token of your application.
  * @param sleepTime Time in minutes to refresh trending topics and restart stream.
  *                  Trending topics are cached for 5 minutes, so you shouldn't set this lower than that.
  *                  Consider that there is some overhead in stopping and restarting the stream.
  */
class TwitterTrendingStatusSource(consumerToken: ConsumerToken,
                                  accessToken: AccessToken,
                                  sleepTime: Int)
    extends RichSourceFunction[TweetWrapper]
    with Logging {

  //amount of time to sleep after each trending call
  val sleepTimeMilli: Long = Time
    .minutes(sleepTime)
    .toMilliseconds

  var isRunning: Boolean = false

  def getRestClient: TwitterRestClient =
    TwitterRestClient(consumerToken, accessToken)
  def getStreamingClient: TwitterStreamingClient =
    TwitterStreamingClient(consumerToken, accessToken)

  override def open(parameters: Configuration) = isRunning = true
  override def cancel(): Unit = isRunning = false

  override def run(ctx: SourceFunction.SourceContext[TweetWrapper]): Unit = {
    var stream: TwitterStream = null

    while (isRunning) {
      //get stream
      val currentTrends = requestTrending()
      logger.info(s"Retrieved new trends: $currentTrends")

      //close the old stream
      if (stream != null) Await.result(stream.close(), 10 seconds)

      //start new stream
      stream = Await.result(startStream(currentTrends, ctx), 10 seconds)

      //time to sleep
      logger.info(s"Started new stream, now sleeping for $sleepTime minute(s).")
      Thread.sleep(sleepTimeMilli)
    }
  }

  /**
    * Starts a stream and collects the tweets to the context.
    *
    * @param trending the trending keywords to filter on.
    * @param ctx      the context to collect to.
    * @return the TwitterStream object wrapped in a future.
    */
  def startStream(trending: List[String],
                  ctx: SourceFunction.SourceContext[TweetWrapper])
    : Future[TwitterStream] = {
    getStreamingClient.filterStatuses(tracks = trending) {
      case tweet: Tweet =>
        ctx.collectWithTimestamp(TweetWrapper(tweet), tweet.created_at.getTime)
      case x => logger.info(x)
    }
  }

  /**
    * Request all the trending topics globally.
    *
    * @return the list of trending topics (empty if none found).
    */
  def requestTrending(): List[String] = {
    val trends = Await.result(getRestClient.globalTrends(), 10 seconds)

    //get and return the trends
    trends match {
      case x: RatedData[Seq[LocationTrends]] =>
        return x.data.flatMap(y => y.trends.map(_.name)).toList
    }

    //return empty list if no trends are found
    List()
  }
}
