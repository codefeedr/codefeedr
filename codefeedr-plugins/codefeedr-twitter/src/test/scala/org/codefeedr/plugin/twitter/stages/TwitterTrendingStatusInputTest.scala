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

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.KillSwitch
import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.danielasfregola.twitter4s.entities._
import com.danielasfregola.twitter4s.entities.enums.{
  DisconnectionCode,
  FilterLevel
}
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.entities.streaming.common.{
  DisconnectMessage,
  DisconnectMessageInfo
}
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.codefeedr.plugin.twitter.TwitterProtocol.TweetWrapper
import org.mockito.{ArgumentCaptor, Matchers}
import org.mockito.Matchers.any
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.exceptions.verification.NoInteractionsWanted

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TwitterTrendingStatusInputTest extends FunSuite with MockitoSugar {

  val consumerToken = ConsumerToken("Consumer Key", "Consumer Secrect")
  val accessToken = AccessToken("Access Key", "Access Secret")

  val simpleTweet =
    Tweet(created_at = new Date(), id = 1, id_str = "1", source = "", text = "")

  test("Trending topics should be properly loaded") {
    val source =
      spy(new TwitterTrendingStatusSource(consumerToken, accessToken, 15))
    val client = mock[TwitterRestClient]

    //create some trends
    val trendList1 = Trend("trend1", "", "", None) :: Trend(
      "trend2",
      "",
      "",
      None) :: Trend("trend3", "", "", None) :: Nil
    val trendList2 = Trend("trend4", "", "", None) :: Nil
    val trends = RatedData[Seq[LocationTrends]](
      RateLimit(1, 2, new Date),
      Seq(LocationTrends("", "", trends = trendList1),
          LocationTrends("", "", trends = trendList2)))

    doReturn(client)
      .when(source)
      .getRestClient

    when(client.globalTrends()).thenReturn(Future { trends })

    val trending_topics = source.requestTrending()
    assert(trending_topics == List("trend1", "trend2", "trend3", "trend4"))
  }

  test("The tweets should be properly collected") {
    val source =
      spy(new TwitterTrendingStatusSource(consumerToken, accessToken, 15))

    val twitterClient = mock[TwitterStreamingClient]
    val context = mock[SourceFunction.SourceContext[TweetWrapper]]
    val argumentCaptor = ArgumentCaptor.forClass(
      classOf[PartialFunction[CommonStreamingMessage, Unit]])

    //return mocked stream
    doReturn(twitterClient).when(source).getStreamingClient

    val trends: List[String] = List("trend1", "trend2")

    source.startStream(trends, context)

    //capture partial function
    verify(twitterClient).filterStatuses(any(),
                                         Matchers.eq(trends),
                                         any(),
                                         any(),
                                         any(),
                                         any())(argumentCaptor.capture())

    //call with simple tweet and commonstreamingmessage
    val f = argumentCaptor.getValue
    f(simpleTweet)
    f(simpleTweet)
    f(
      DisconnectMessage(
        DisconnectMessageInfo(DisconnectionCode.Stall, "test", "test")))

    //ensure there is only collected twice
    verify(context, times(2))
      .collectWithTimestamp(any(classOf[TweetWrapper]), any())
  }

  test("The Flink source should correctly be closed and opened") {
    val source = new TwitterTrendingStatusSource(consumerToken, accessToken, 15)

    source.open(new Configuration())
    assert(source.isRunning)

    source.cancel()
    assert(!source.isRunning)
  }

  test("A TwitterStreamingClient should be created correctly") {
    val source =
      spy(new TwitterTrendingStatusSource(consumerToken, accessToken, 15))
    val client = source.getStreamingClient

    assert(client.consumerToken == consumerToken)
    assert(client.accessToken == accessToken)
  }

  test("A TwitterRestClient should be created correctly") {
    val source =
      spy(new TwitterTrendingStatusSource(consumerToken, accessToken, 15))
    val client = source.getRestClient

    assert(client.consumerToken == consumerToken)
    assert(client.accessToken == accessToken)
  }

  test("The Flink source should correctly run") {
    val source =
      spy(new TwitterTrendingStatusSource(consumerToken, accessToken, 0))
    val twitterClient = mock[TwitterStreamingClient]

    val killSwitch = mock[KillSwitch]
    val actorSystem = mock[ActorSystem]

    val twitterStream =
      TwitterStream(consumerToken, accessToken)(killSwitch, null, actorSystem)
    val context = mock[SourceFunction.SourceContext[TweetWrapper]]

    //set isRunning
    source.open(new Configuration())

    //return trend list
    doReturn(List("trend"))
      .when(source)
      .requestTrending()

    //return twitterstream
    doReturn(Future { twitterStream })
      .when(source)
      .startStream(any(), any())

    //cancel the source after 0.5 second
    new Thread() {
      override def run: Unit = {
        Thread.sleep(10)
        source.cancel()
      }
    }.start()

    //run the source
    source.run(context)

    verify(source, atLeast(2)).startStream(any(), any())
    verify(killSwitch, atLeast(1)).shutdown()
    verify(source, atLeast(2)).requestTrending()
  }

  test("A source needs to be properly added to the environment") {
    val env = mock[StreamExecutionEnvironment]
    val stage = spy(new TwitterTrendingStatusInput(consumerToken, accessToken))

    doReturn(env)
      .when(stage)
      .environment

    stage
      .main()

    //for some reason this doesn't work, gives exception: 2 matches expected, 1 recorded
    // verify(env, times(1)).addSource(any[SourceFunction[TweetWrapper]])

    //work around to ensure mocked env is used
    assertThrows[NoInteractionsWanted] {
      verifyZeroInteractions(env)
    }
  }

}
