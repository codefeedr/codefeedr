package org.codefeedr.plugins.ghtorrent.util

import java.util

import com.rabbitmq.client._
import org.apache.flink.api.common.serialization.{
  DeserializationSchema,
  SimpleStringSchema
}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.ResultTypeQueryable
import org.apache.flink.streaming.api.functions.source.{
  MultipleIdsMessageAcknowledgingSourceBase,
  SourceFunction
}
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

class GHTorrentRMQSource(username: String,
                         routingKeysFile: String = "routing_keys.txt",
                         usesCorrelationId: Boolean = false)
    extends MultipleIdsMessageAcknowledgingSourceBase[String, String, Long](
      classOf[String])
    with ResultTypeQueryable[String] {

  private val serialVersionUID: Long = 1L
  private val LOG: Logger = LoggerFactory.getLogger(classOf[GHTorrentRMQSource])
  private val schema: SimpleStringSchema = new SimpleStringSchema()
  private val rmConnectionConfig: RMQConnectionConfig =
    new RMQConnectionConfig.Builder()
      .setHost("localhost")
      .setUserName("streamer")
      .setPassword("streamer")
      .build()

  @transient
  protected var connection: Connection = null
  protected var channel: Channel = null
  protected var consumer: DefaultConsumer = null

  /** Parse all routing keys from the file. We assume they are separated by new lines. **/
  val routingKeys = parseRoutingKeys()

  /** Name of the exchange, this is a requirement by the GHTorrent streaming service. **/
  val exchangeName = "ght-streams"

  /**
    * Setups queue according to http://ghtorrent.org/streaming.html
    */
  def setupQueue(): Unit = {
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

  override def acknowledgeSessionIDs(sessionIds: util.List[Long]): Unit = ???

  override def getProducedType: TypeInformation[String] = ???

  override def run(ctx: SourceFunction.SourceContext[String]): Unit = ???

  override def cancel(): Unit = ???

  /** Parses all routing keys from the file.
    *
    * @return a list of routing keys.
    */
  private def parseRoutingKeys(): List[String] =
    Source.fromResource(routingKeysFile).mkString.split("\n").toList
}
