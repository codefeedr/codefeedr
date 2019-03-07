package org.codefeedr.plugins.ghtorrent.util

import org.apache.flink.api.common.serialization.DeserializationSchema
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig

class GHTorrentRMQSource[T](rmqConnectionConfig: RMQConnectionConfig,
                            queueName: String,
                            deserSchema: DeserializationSchema[T])
    extends RMQSource[T](rmqConnectionConfig, queueName, deserSchema) {

  override def setupQueue(): Unit = {
    //setup queue according to http://ghtorrent.org/streaming.html
  }
}
