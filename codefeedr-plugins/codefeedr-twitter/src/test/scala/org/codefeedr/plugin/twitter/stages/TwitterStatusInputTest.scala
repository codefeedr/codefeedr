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

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.enums.DisconnectionCode.DisconnectionCode
import com.danielasfregola.twitter4s.entities.enums.{
  DisconnectionCode,
  FilterLevel
}
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.entities.streaming.common.{
  DisconnectMessage,
  DisconnectMessageInfo
}
import com.danielasfregola.twitter4s.entities.{
  AccessToken,
  ConsumerToken,
  Tweet
}
import com.danielasfregola.twitter4s.http.clients.streaming.TwitterStream
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.codefeedr.plugin.twitter.TwitterProtocol.TweetWrapper
import org.mockito.{ArgumentCaptor, Matchers}
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.codefeedr.pipeline.Context
import org.mockito.exceptions.verification.NoInteractionsWanted

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TwitterStatusInputTest extends FunSuite with MockitoSugar {

  val consumerToken = ConsumerToken("Consumer Key", "Consumer Secrect")
  val accessToken = AccessToken("Access Key", "Access Secret")

  val simpleTweet =
    Tweet(created_at = new Date(), id = 1, id_str = "1", source = "", text = "")

  test("The Flink source should correctly be closed and opened") {
    val source = new TwitterStatusSource(consumerToken,
                                         accessToken,
                                         Seq.empty,
                                         Seq.empty,
                                         Seq.empty,
                                         Seq.empty,
                                         false,
                                         FilterLevel.None)

    source.open(new Configuration())
    assert(source.isRunning)

    source.cancel()
    assert(!source.isRunning)
  }

  test("The tweets should be properly collected") {
    val source = spy(
      new TwitterStatusSource(consumerToken,
                              accessToken,
                              Seq.empty,
                              Seq.empty,
                              Seq.empty,
                              Seq.empty,
                              false,
                              FilterLevel.None))

    val twitterClient = mock[TwitterStreamingClient]
    val context = mock[SourceFunction.SourceContext[TweetWrapper]]
    val argumentCaptor = ArgumentCaptor.forClass(
      classOf[PartialFunction[CommonStreamingMessage, Unit]])

    //return mocked stream
    doReturn(twitterClient).when(source).getClient

    //open the source
    source.open(new Configuration())

    //cancel the source after 1 second
    new Thread() {
      override def run: Unit = {
        Thread.sleep(1000)
        source.cancel()
      }
    }.start()

    //run the source
    source.run(context)

    //capture partial function
    verify(twitterClient).filterStatuses(any(),
                                         any(),
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

    //ensure there is only collected once
    verify(context, times(2)).collect(any(classOf[TweetWrapper]))
  }

  test("A source needs to be properly added to the environment") {
    val env = mock[StreamExecutionEnvironment]
    val context = Context(env, "", null, null)
    val stage = spy(new TwitterStatusInput(consumerToken, accessToken))

    doReturn(context)
      .when(stage)
      .getContext

    stage
      .main(context)

    //for some reason this doesn't work, gives exception: 2 matches expected, 1 recorded
    // verify(env, times(1)).addSource(any[SourceFunction[TweetWrapper]])

    //work around to ensure mocked env is used
    assertThrows[NoInteractionsWanted] {
      verifyZeroInteractions(env)
    }
  }

  test("A TwitterClient should be created correctly") {
    val source = spy(
      new TwitterStatusSource(consumerToken,
                              accessToken,
                              Seq.empty,
                              Seq.empty,
                              Seq.empty,
                              Seq.empty,
                              false,
                              FilterLevel.None))
    val client = source.getClient

    assert(client.consumerToken == consumerToken)
    assert(client.accessToken == accessToken)
  }

}
