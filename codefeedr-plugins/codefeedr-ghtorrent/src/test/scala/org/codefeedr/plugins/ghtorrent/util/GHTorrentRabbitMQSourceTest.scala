package org.codefeedr.plugins.ghtorrent.util

import java.io.IOException
import java.util

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

class GHTorrentRabbitMQSourceTest extends FunSuite with MockitoSugar {

  test("RabbitMQ is correctly configured with host and port.") {
    val source = new GHTorrentRMQSource(username = "username",
                                        host = "host_name",
                                        port = 0)

    assert(source.rmConnectionConfig.getHost == "host_name")
    assert(source.rmConnectionConfig.getPort == 0)
  }

  test("Correct routing_keys are loaded.") {
    val source =
      new GHTorrentRMQSource(username = "test", routingKeysFile = "routing.txt")

    assert(source.routingKeys.length == 3)
    assert(source.routingKeys.contains("a"))
    assert(source.routingKeys.contains("b"))
    assert(source.routingKeys.contains("d"))
  }

  test("Queue should be properly setup.") {
    val source = new GHTorrentRMQSource(username = "username",
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

    val source = new GHTorrentRMQSource(username = "username")
    source.connection = mockedConnection
    when(mockedConnection.close()).thenThrow(new IOException())

    assertThrows[RuntimeException] {
      source.close()
    }
  }

  test("Source should throw runtime if channel is null.") {
    val config = new Configuration()
    val mockedConnection = mock[Connection]

    val source = new GHTorrentRMQSource(username = "username")
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
    val source = spy(new GHTorrentRMQSource(username = "username"))
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

    val source = new GHTorrentRMQSource(username = "username")
    source.connection = mockedConnection
    source.channel = mockedChannel

    source.setRuntimeContext(mockedContext)
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
    val source = spy(new GHTorrentRMQSource(username = "username"))
    doReturn(mockedContext).when(source).getRuntimeContext
    doReturn(mockedFactory).when(source).getFactory()
    when(mockedFactory.newConnection()).thenThrow(new IOException())

    assertThrows[RuntimeException] {
      source.open(config)
    }
  }

}
