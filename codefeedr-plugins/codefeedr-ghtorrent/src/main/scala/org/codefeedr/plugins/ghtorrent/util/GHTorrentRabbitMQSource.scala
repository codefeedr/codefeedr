package org.codefeedr.plugins.ghtorrent.util

import com.rabbitmq.client.ConnectionFactory
import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record

class GHTorrentRMQSource(deserSchema: DeserializationSchema[Record])
    extends RMQSource[Record](
      new RMQConnectionConfig.Builder()
        .setHost("localhost")
        .setUserName("streamer")
        .setPassword("streamer")
        .build(),
      "", //Set the queue_name to "" since we are going to override it anyways.
      deserSchema
    ) {

  /** Name of the exchange, this is a requirement by the GHTorrent streaming service. **/
  val exchangeName = "ght-streams"

  override def setupQueue(): Unit = {
    //setup queue according to http://ghtorrent.org/streaming.html
  }
}
