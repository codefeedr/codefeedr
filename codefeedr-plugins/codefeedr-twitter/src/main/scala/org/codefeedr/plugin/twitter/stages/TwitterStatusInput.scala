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

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{
  AccessToken,
  ConsumerToken,
  Tweet
}
import com.danielasfregola.twitter4s.entities.enums.FilterLevel
import com.danielasfregola.twitter4s.entities.enums.FilterLevel.FilterLevel
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import org.apache.flink.api.scala._
import grizzled.slf4j.Logging
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{
  RichSourceFunction,
  SourceFunction
}
import org.apache.flink.streaming.api.scala.DataStream
import org.codefeedr.pipeline.Context
import org.codefeedr.plugin.twitter.TwitterProtocol.TweetWrapper
import org.codefeedr.stages.InputStage

import scala.concurrent.blocking

/**
  * InputStage which retrieves tweets based on some filters.
  *
  * @param consumerToken  Consumer Token of your application.
  * @param accessToken    Access Token of your application.
  * @param follow         List of user IDs, indicating the users whose Tweets should be delivered on the stream.
  * @param tracks         List of phrases which will be used to determine what Tweets will be delivered on the stream.
  * @param locations      Specifies a set of bounding boxes to track.
  * @param languages      List of 'BCP 47' language identifiers.
  * @param stall_warnings Specifies whether stall warnings (`WarningMessage`) should be delivered as part of the updates.
  * @param filter_level   Set the minimum value of the filter_level Tweet attribute required to be included in the stream.
  */
class TwitterStatusInput(consumerToken: ConsumerToken,
                         accessToken: AccessToken,
                         follow: Seq[Long] = Seq.empty,
                         tracks: Seq[String] = Seq.empty,
                         locations: Seq[Double] = Seq.empty,
                         languages: Seq[Language] = Seq.empty,
                         stall_warnings: Boolean = false,
                         filter_level: FilterLevel = FilterLevel.None,
                         stageId: Option[String] = None)
    extends InputStage[TweetWrapper](stageId) {

  def main(context: Context): DataStream[TweetWrapper] = {
    context.env.addSource(
      new TwitterStatusSource(consumerToken,
                              accessToken,
                              follow,
                              tracks,
                              locations,
                              languages,
                              stall_warnings,
                              filter_level))
  }
}

/**
  * Flink source which retrieves tweets based on some filters.
  *
  * @param consumerToken  Consumer Token of your application.
  * @param accessToken    Access Token of your application.
  * @param follow         List of user IDs, indicating the users whose Tweets should be delivered on the stream.
  * @param tracks         List of phrases which will be used to determine what Tweets will be delivered on the stream.
  * @param locations      Specifies a set of bounding boxes to track.
  * @param languages      List of 'BCP 47' language identifiers.
  * @param stall_warnings Specifies whether stall warnings (`WarningMessage`) should be delivered as part of the updates.
  * @param filter_level   Set the minimum value of the filter_level Tweet attribute required to be included in the stream.
  */
class TwitterStatusSource(consumerToken: ConsumerToken,
                          accessToken: AccessToken,
                          follow: Seq[Long],
                          tracks: Seq[String],
                          locations: Seq[Double],
                          languages: Seq[Language],
                          stall_warnings: Boolean,
                          filter_level: FilterLevel)
    extends RichSourceFunction[TweetWrapper]
    with Logging {

  val SLEEP_TIME: Int = 1000
  var isRunning: Boolean = false

  /**
    * Retrieves a new TwitterStream instance.
    *
    * @return a new TwitterStream.
    */
  def getClient: TwitterStreamingClient =
    TwitterStreamingClient(consumerToken, accessToken)

  /**
    * Opens the source function.
    *
    * @param parameters
    */
  override def open(parameters: Configuration): Unit = {
    isRunning = true
  }

  /**
    * Cancels the source function and stops the stream.
    */
  override def cancel(): Unit = {
    isRunning = false
  }

  /**
    * Starts and opens a twitter stream retrieving statuses.
    *
    * @param ctx the context to collect to.
    */
  override def run(ctx: SourceFunction.SourceContext[TweetWrapper]): Unit = {
    getClient.filterStatuses(follow,
                             tracks,
                             locations,
                             languages,
                             stall_warnings,
                             filter_level) {
      case tweet: Tweet => ctx.collect(TweetWrapper(tweet))
      case x            => logger.info(s"[twitter] $x")
    }

    while (isRunning) {
      blocking {
        Thread.sleep(SLEEP_TIME) //sleep in order to keep the stream alive
      }
    }
  }

}
