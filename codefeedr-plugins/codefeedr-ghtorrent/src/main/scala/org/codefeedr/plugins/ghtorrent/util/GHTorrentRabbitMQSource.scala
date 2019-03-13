package org.codefeedr.plugins.ghtorrent.util

import java.util

import com.rabbitmq.client.ConnectionFactory
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record

import scala.io.Source

class GHTorrentRMQSource(username: String,
                         routingKeysFile: String = "routing_keys.txt",
                         deserSchema: DeserializationSchema[Record])
    extends RMQSource[Record](
      new RMQConnectionConfig.Builder()
        .setHost("localhost")
        .setUserName("streamer")
        .setPassword("streamer")
        .build(),
      "", //Set the queue_name to "" since we are going to override it anyways.
      deserSchema
    ) {

  /** Parse all routing keys from the file. We assume they are separated by new lines. **/
  val routingKeys = parseRoutingKeys()

  /** Name of the exchange, this is a requirement by the GHTorrent streaming service. **/
  val exchangeName = "ght-streams"

  /**
    * Setups queue according to http://ghtorrent.org/streaming.html
    */
  override def setupQueue(): Unit = {
    // First of all, we declare an exchange with the correct name and type.
    channel.exchangeDeclare(exchangeName, "topic", true)

    // Create a queue with auto delete.
    val queue = channel.queueDeclare(username + "_queue",
                                     false,
                                     false,
                                     true,
                                     new util.HashMap[String, AnyRef]())

    // For each routing key, bind it to the channel.
    val queueName = queue.getQueue()
    routingKeys.foreach(channel.queueBind(queueName, exchangeName, _))
  }

  /** Parses all routing keys from the file.
    *
    * @return a list of routing keys.
    */
  private def parseRoutingKeys(): List[String] =
    Source.fromResource(routingKeysFile).mkString.split("\n").toList
}
