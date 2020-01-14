package org.codefeedr.plugins.ghtorrent.util

import java.io.IOException
import java.util

import com.rabbitmq.client._
import org.apache.flink.api.common.typeinfo.{BasicTypeInfo, TypeInformation}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext
import org.mockito.ArgumentCaptor
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.stubbing.Answer

class GHTorrentRabbitMQSourceTest extends FunSuite with MockitoSugar {

  test("RabbitMQ is correctly configured with host and port.") {
    val source = new GHTorrentRabbitMQSource(username = "username",
                                             host = "host_name",
                                             port = 0)

    assert(source.rmConnectionConfig.getHost == "host_name")
    assert(source.rmConnectionConfig.getPort == 0)
  }

  test("Correct routing_keys are loaded.") {
    val source =
      new GHTorrentRabbitMQSource(username = "test",
                                  routingKeysFile = "routing.txt")

    assert(source.routingKeys.length == 3)
    assert(source.routingKeys.contains("a"))
    assert(source.routingKeys.contains("b"))
    assert(source.routingKeys.contains("d"))
  }

  test("Queue should be properly setup.") {
    val source = new GHTorrentRabbitMQSource(username = "username",
                                             routingKeysFile = "routing.txt")
    val mockChannel = mock[Channel]

    source.channel = mockChannel
    val exchangeName = "ght-streams"
    val queueName = "username_queue"

    source.setupQueue()

    verify(mockChannel).exchangeDeclare(exchangeName, "topic", true)
    verify(mockChannel).queueDeclare(queueName,
                                     false,
                                     false,
                                     true,
                                     new util.HashMap[String, AnyRef]())
    verify(mockChannel, times(3))
      .queueBind(any[String], any[String], any[String])
  }

  test("Connection throws IOException while closing") {
    val mockedConnection = mock[Connection]

    val source = new GHTorrentRabbitMQSource(username = "username")
    source.connection = mockedConnection
    when(mockedConnection.close()).thenThrow(new IOException())

    assertThrows[RuntimeException] {
      source.close()
    }
  }

  test("Source should throw runtime if channel is null.") {
    val config = new Configuration()
    val mockedConnection = mock[Connection]

    val source = new GHTorrentRabbitMQSource(username = "username")
    source.connection = mockedConnection
    source.channel = null

    assertThrows[RuntimeException] {
      source.open(config)
    }
  }

  test("Source should disable autoAck if checkpointing is enabled.") {
    val config = new Configuration()
    val mockedConnection = mock[Connection]
    val mockedChannel = mock[Channel]
    val mockedFactory = mock[ConnectionFactory]
    val mockedContext = mock[StreamingRuntimeContext]

    // a lot of mockito magic
    when(mockedContext.isCheckpointingEnabled).thenReturn(true)
    val source = spy(new GHTorrentRabbitMQSource(username = "username"))
    doReturn(mockedContext).when(source).getRuntimeContext
    doReturn(mockedFactory).when(source).getFactory()
    when(mockedFactory.newConnection).thenReturn(mockedConnection)
    when(mockedConnection.createChannel()).thenReturn(mockedChannel)

    source.connection = mockedConnection

    source.open(config)

    verify(mockedChannel).txSelect()
    assert(!source.autoAck)
  }

  test("Source should enable autoAck if checkpointing is disabled.") {
    val config = new Configuration()
    val mockedConnection = mock[Connection]
    val mockedChannel = mock[Channel]
    val mockedContext = mock[StreamingRuntimeContext]
    val mockedFactory = mock[ConnectionFactory]

    // a lot of mockito magic
    when(mockedContext.isCheckpointingEnabled).thenReturn(false)
    val source = spy(new GHTorrentRabbitMQSource(username = "username"))
    doReturn(mockedContext).when(source).getRuntimeContext
    doReturn(mockedFactory).when(source).getFactory()
    when(mockedFactory.newConnection).thenReturn(mockedConnection)
    when(mockedConnection.createChannel()).thenReturn(mockedChannel)

    source.connection = mockedConnection
    source.open(config)
    assert(source.autoAck)
  }

  test(
    "Source should thrown runtime when something goes wrong during setting up RMQ.") {
    val config = new Configuration()
    val mockedConnection = mock[Connection]
    val mockedChannel = mock[Channel]
    val mockedFactory = mock[ConnectionFactory]
    val mockedContext = mock[StreamingRuntimeContext]

    // a lot of mockito magic
    when(mockedContext.isCheckpointingEnabled).thenReturn(true)
    val source = spy(new GHTorrentRabbitMQSource(username = "username"))
    doReturn(mockedContext).when(source).getRuntimeContext
    doReturn(mockedFactory).when(source).getFactory()
    when(mockedFactory.newConnection()).thenThrow(new IOException())

    assertThrows[RuntimeException] {
      source.open(config)
    }
  }

  test("Produced type should be a String") {
    assert(
      new GHTorrentRabbitMQSource("").getProducedType == BasicTypeInfo.STRING_TYPE_INFO)
  }

  test("acknowledgeSessionsIds could catch an IOException") {
    val source = new GHTorrentRabbitMQSource("")
    val channel = mock[Channel]
    source.channel = channel

    when(channel.txCommit()).thenThrow(new IOException())

    assertThrows[RuntimeException] {
      source.acknowledgeSessionIDs(new util.ArrayList[Long]())
    }
  }

  test("RabbitMQSource should properly run with autoack") {
    val source = new GHTorrentRabbitMQSource("")
    val channel = mock[Channel]
    val context = mock[SourceFunction.SourceContext[String]]
    val captor = ArgumentCaptor.forClass(classOf[DefaultConsumer])

    doReturn(new Object()).when(context).getCheckpointLock

    source.channel = channel
    source.autoAck = true
    source.run(context)

    verify(channel).basicConsume(any[String],
                                 any[Boolean],
                                 any[String],
                                 captor.capture())

    val consumer = captor.getValue
    val mockEnv = mock[Envelope]
    when(mockEnv.getRoutingKey).thenReturn("test")
    consumer.handleDelivery("", mockEnv, null, Array())
    verify(context).collect("test#")
  }

  test("RabbitMQSource should properly run without autoack") {
    val source = spy(new GHTorrentRabbitMQSource(""))
    val channel = mock[Channel]
    val context = mock[SourceFunction.SourceContext[String]]
    val captor = ArgumentCaptor.forClass(classOf[DefaultConsumer])
    val mockSessionIds = mock[java.util.ArrayList[Long]]
    doReturn(new Object()).when(context).getCheckpointLock
    doReturn(mockSessionIds).when(source).getSessionIds

    source.channel = channel
    source.autoAck = false

    source.run(context)

    verify(channel).basicConsume(any[String],
                                 any[Boolean],
                                 any[String],
                                 captor.capture())

    val consumer = captor.getValue
    val mockEnv = mock[Envelope]

    when(mockEnv.getRoutingKey).thenReturn("test")
    consumer.handleDelivery("", mockEnv, null, Array())
    verify(context).collect("test#")
    verify(mockSessionIds).add(any[Long])
  }

//  test("RabbitMQSource should properly run without autoack with correlation id") {
//    val source = spy(new GHTorrentRabbitMQSource("", usesCorrelationId = true))
//    val channel = mock[Channel]
//    val context = mock[SourceFunction.SourceContext[String]]
//    val captor = ArgumentCaptor.forClass(classOf[DefaultConsumer])
//    val mockSessionIds = mock[java.util.ArrayList[Long]]
//    val mockProps = mock[AMQP.BasicProperties]
//    doReturn(new Object()).when(context).getCheckpointLock
//    doReturn(mockSessionIds).when(source).getSessionIds
//
//    doReturn(false).when(source).alreadyProcessed(any[String])
//
//    when(mockProps.getCorrelationId).thenReturn("corId")
//
//    source.channel = channel
//    source.autoAck = false
//
//    source.run(context)
//
//    verify(channel).basicConsume(any[String],
//                                 any[Boolean],
//                                 any[String],
//                                 captor.capture())
//
//    val consumer = captor.getValue
//    val mockEnv = mock[Envelope]
//
//    when(mockEnv.getRoutingKey).thenReturn("test")
//    consumer.handleDelivery("", mockEnv, mockProps, Array())
//    verify(context, times(0)).collect(any[String])
//    verify(mockSessionIds, times(0)).add(any[Long])
//  }

  test("Verify some getters") {
    val source = new GHTorrentRabbitMQSource("")

    source.cancel()

    assert(source.getSessionIds == null)

    assertThrows[NullPointerException] {
      source.alreadyProcessed("")
    }
  }

}
