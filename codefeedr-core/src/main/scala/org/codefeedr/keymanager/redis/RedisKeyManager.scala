/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codefeedr.keymanager.redis

import java.net.URI
import java.util.Date

import com.redis._
import org.codefeedr.keymanager.{KeyManager, ManagedKey}

/**
  * A key manager using Redis, supporting refresh intervals and call counting.
  *
  * <h3>Adding keys to Redis</h3>
  * When adding a key to Redis, a refresh policy is required:
  *
  * <i>Key</i> is the API key to store
  * <i>Limit</i> is the number of calls the key allows within a time interval
  * <i>Interval</i> is the number of milliseconds between two resets of the use count.
  * <i>Time</i> is the next time the key counter should reset.
  *
  * When, for example, a key has 1000 calls every day, one would use:
  * <pre>limit = 1000, interval = 60 * 60 * 24 * 1000, time = next midnight</pre>
  *
  *
  * <pre>
  * targetKey = [root]:[target]
  * ZADD [targetKey]:keys [limit] [key]
  * ZADD [targetKey]:refreshTime [time] [key]
  * HSET [targetKey]:limit [key] [limit]
  * HSET [targetKey]:interval [key] [interval]
  * </pre>
  *
  * <h3>Deleting keys from Redis</h3>
  *
  * <pre>
  * targetKey = [root]:[target]
  * ZREM [targetKey]:keys key
  * ZREM [targetKey]:refreshTime key
  * HDEL [targetKey]:limit key
  * HDEL [targetKey]:interval key
  * </pre>
  *
  * @param host Hostname and port of Redis. Use the redis scheme: 'redis://localhost:6379'
  * @param root Root path of the key manager within Redis. Defaults to 'codefeedr:keymanager'
  */
class RedisKeyManager(host: String, root: String = "codefeedr:keymanager") extends KeyManager {
  private var connection: RedisClient = _
  private var scriptId: String = _

  connect()

  /**
    * Start a new connection to Redis and configure it properly.
    */
  private def connect(): Unit = {
    val uri = new URI(host)
    connection = new RedisClient(uri)

    scriptId = connection.scriptLoad(getRequestLuaScript).get
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
    * Get the Redis key for given target.
    * @param target Target of the key pool
    * @return Target key
    */
  private def redisKeyForTarget(target: String): String = root + ":" + target

  override def request(target: String, numberOfCalls: Int): Option[ManagedKey] = {
    require(target != null, "target cannot be null")

    import serialization.Parse.Implicits.parseString

    val targetKey = redisKeyForTarget(target)
    val time = new Date().getTime

    // Run the custom script for a fully atomic get+decr operation
    val result: Option[List[Option[String]]] = connection.evalMultiSHA(scriptId, List(targetKey), List(numberOfCalls, time))

    val data = result.get
    if (data.isEmpty)
      None
    else
      Some(ManagedKey(data.head.get, data.last.get.toInt))
  }

  private[redis] def disconnect(): Unit = {
    connection.disconnect
    connection = null
  }

  /**
    * Add a new key to redis for testing.
    *
    * @param target Key target
    * @param key Key
    * @param numCalls Number of calls allowed within interval
    * @param interval Interval in milliseconds
    */
  private[redis] def set(target: String, key: String, numCalls: Int, interval: Int): Unit = {
    val targetKey = redisKeyForTarget(target)
    val time = new Date().getTime + interval

    connection.zadd(targetKey + ":keys", numCalls, key)
    connection.zadd(targetKey + ":refreshTime", time, key)
    connection.hset(targetKey + ":limit", key, numCalls)
    connection.hset(targetKey + ":interval", key, interval)
  }

  private[redis] def deleteAll(): Unit = {
    connection.evalBulk("for _,k in ipairs(redis.call('keys',ARGV[1])) do redis.call('del',k) end", List(), List(root + ":*"))
  }

}
