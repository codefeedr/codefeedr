package org.codefeedr.keymanager.redis

import java.net.URI
import java.util.Date

import com.redis._
import org.codefeedr.keymanager.KeyManager

class RedisKeyManager(host: String, root: String = "codefeedr:keymanager") extends KeyManager {
  private var connection: RedisClient = _
  private var requestScriptId: String = _

  /**
    * Start a new connection to Redis and configure it properly.
    *
    * @throws RuntimeException
    */
  private def connect(): Unit = {
    val uri = new URI(host)
    connection = new RedisClient(uri)

    val sha = connection.scriptLoad(getRequestLuaScript)
    if (sha.isDefined)
      requestScriptId = sha.get
  }

  /**
    * Custom script for getting a key from the KV-store.
    *
    * @return script
    */
  private def getRequestLuaScript: String = {
    val stream = getClass.getResourceAsStream("/redis_request.lua")

    scala.io.Source.fromInputStream(stream).mkString
  }

  /**
    * Get whether there is an active connection
    * @return has active connection
    */
  private def isConnected: Boolean = connection != null

  /**
    * Get the Redis key for given target.
    * @param target Target of the key pool
    * @return Target key
    */
  private def redisKeyForTarget(target: String): String = root + ":" + target

  override def request(target: String, numberOfCalls: Int): Option[String] = {
    import serialization.Parse.Implicits.parseString

    if (!isConnected)
      connect()

    val targetKey = redisKeyForTarget(target)

    // Run the custom script for a fully atomic get+decr operation
    val result: Option[List[Option[String]]] = connection.evalMultiSHA(requestScriptId, List(targetKey), List(numberOfCalls))

    if (result.isEmpty)
      return None

    val data = result.get
    if (data.isEmpty)
      None
    else
      data.head
  }

  ///////// Methods for testing

  private[redis] def disconnect(): Unit = {
    connection.disconnect
    connection = null
  }

  private[redis] def set(target: String, key: String, numCalls: Int): Unit = {
    if (!isConnected)
      connect()

    val targetKey = redisKeyForTarget(target)
    connection.zadd(targetKey + ":keys", numCalls, key)

    // TODO: refresh policy
    connection.hset(targetKey + ":lastRefresh", key, new Date().getTime)
    //    connection.hset(targetKey + ":policy"
  }

  private[redis] def get(target: String, key: String): Option[Int] = {
    if (!isConnected)
      connect()

    val targetKey = redisKeyForTarget(target)
    val result = connection.zscore(targetKey + ":keys", key)

    if (result.isEmpty)
      None
    else
      Some(result.get.toInt)
  }

  private[redis] def delete(target: String, key: String): Unit = {
    if (!isConnected)
      connect()

    val targetKey = redisKeyForTarget(target)

    connection.zrem(targetKey + ":keys", key)

    // TODO: refresh policy
    connection.hdel(targetKey + ":lastRefresh", key)
  }

  private[redis] def deleteAll(): Unit = {
    if (!isConnected)
      connect()

    connection.evalBulk("for _,k in ipairs(redis.call('keys',ARGV[1])) do redis.call('del',k) end", List(), List(root + ":*"))
  }

}
