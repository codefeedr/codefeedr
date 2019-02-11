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
 */
package org.codefeedr.keymanager.redis

import java.net.URI
import java.util.Date

import com.github.sebruck.EmbeddedRedis
import com.redis.RedisClient
import org.codefeedr.keymanager.KeyManagerTest
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite, PrivateMethodTester}
import redis.embedded.RedisServer

class RedisKeyManagerTest extends KeyManagerTest()
  with BeforeAndAfter
  with PrivateMethodTester
  with EmbeddedRedis
  with BeforeAndAfterAll {

  var km: RedisKeyManager = _
  var redis: RedisServer = null
  var redisPort: Int = 0

  // Before all tests, setup an embedded redis
  override def beforeAll() = {
    redis = startRedis()
    redisPort = redis.ports().get(0)
  }

  // After all tests, stop embedded redis
  override def afterAll() = {
    stopRedis(redis)
  }

  before {
    km = new RedisKeyManager(s"redis://localhost:$redisPort", "cf_test")
    this.injectKeyManager(km)
  }

  after {
    km.deleteAll()
    km.disconnect()
  }

  test("A set key should be retrievable") {
    km.set("testTarget", "testKey", 10, 10000)

    val key = km.request("testTarget", 1)

    assert(key.isDefined)
    assert(key.get.value == "testKey")
  }

  test("An empty store should return no keys") {
    val key = km.request("testTarget", 1)

    assert(key.isEmpty)
  }

  test("The key with the best fitting number of calls should be used") {
    km.set("testTarget", "testKey", 10, 10000)
    km.set("testTarget", "testKey2", 4, 10000)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get.value == "testKey2")
  }

  test("After getting a key the number of calls remaining should be lowered") {
    km.set("testTarget", "testKey", 10, 10000)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get.value == "testKey")
    assert(key.get.remainingCalls == (10 - 3))
  }

  test("Keys should refresh after interval") {
    km.set("testTarget", "testKey", 10, 500)

    km.request("testTarget", 3)

    // Wait 2 seconds to make the keys pass
    Thread.sleep(2000)

    val key = km.request(target = "testTarget", numberOfCalls = 8)

    assert(key.isDefined)
    assert(key.get.value == "testKey")
  }

  test("Refreshing should reset number of calls to limit") {
    km.set("testTarget", "testKey", 15, 500)
    km.request("testTarget", 15)

    Thread.sleep(2000)

    // Refresh is only needed after new key
    val key = km.request("testTarget", 1)

    assert(key.isDefined)
    assert(key.get.remainingCalls == (15 - 1))
  }

  test("RedisKeyManager has a valid default root") {
    val km = new RedisKeyManager(s"redis://localhost:$redisPort")
    val key = km.request("randomTarget", 1)

    assert(key.isEmpty)
  }

  test("When a key is refreshed, the new refreshTime should be in the future") {
    km.set("testTarget", "testKey", 10, 800)

    Thread.sleep(3000)

    val now = new Date().getTime
    val key = km.request("testTarget", 1)

    assert(key.isDefined)
    assert(key.get.remainingCalls == (10 - 1))

    // As per documentation
    val uri = new URI(s"redis://localhost:$redisPort")
    val rc = new RedisClient(uri)
    val refreshTime = rc.zscore("cf_test:testTarget:refreshTime", "testKey")
    rc.disconnect

    assert(refreshTime.get > now)
  }
}
