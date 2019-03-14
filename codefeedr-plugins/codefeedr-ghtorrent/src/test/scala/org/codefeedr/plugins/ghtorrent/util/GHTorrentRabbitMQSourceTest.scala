package org.codefeedr.plugins.ghtorrent.util

import java.util

import com.rabbitmq.client.Channel
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
}
