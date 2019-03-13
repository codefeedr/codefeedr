package org.codefeedr.plugins.ghtorrent.protocol

object GHTorrent {

  case class Record(routingKey: String, contents: String)
}
