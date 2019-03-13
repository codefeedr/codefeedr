package org.codefeedr.plugins.ghtorrent.protocol

object GHTorrent {

  /** Represents a record as retrieved from GHTorrent.
    *
    * @param routingKey the routing key (e.g. evt.push.insert)
    * @param contents the content of the queue record.
    */
  case class Record(routingKey: String, contents: String)
}
