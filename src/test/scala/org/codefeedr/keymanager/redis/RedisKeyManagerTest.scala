package org.codefeedr.keymanager.redis

import org.scalatest.{BeforeAndAfter, FunSuite, PrivateMethodTester}

class RedisKeyManagerTest extends FunSuite
  with BeforeAndAfter
  with PrivateMethodTester {

  var km: RedisKeyManager = _

  before {
    km = new RedisKeyManager("redis://localhost:6379", "cf_test")
  }

  after {
    km.deleteAll()
    km.disconnect()
  }

  test("A set key should be retrievable" ) {
    km.set("testTarget", "testKey", 10, 10000)

    val key = km.request("testTarget", 1)

    assert(key.isDefined)
    assert(key.get == "testKey")
  }

  test("An empty store should return no keys") {
    val key = km.request("testTarget", 1)

    assert(key.isEmpty)
  }

  test("The key with the best fitting number of calls should be used" ) {
    km.set("testTarget", "testKey", 10, 10000)
    km.set("testTarget", "testKey2", 4, 10000)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get == "testKey2")
  }

  test("After getting a key the number of calls remaining should be lowered") {
    km.set("testTarget", "testKey", 10, 10000)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get == "testKey")

    val remaining = km.get("testTarget", "testKey")

    assert(remaining.isDefined)
    assert(remaining.get == (10 - 3))
  }

  test("Keys should refresh after interval") {
    km.set("testTarget", "testKey", 10, 1000)

    km.request("testTarget", 3)

    // Wait 5 seconds to make the keys pass
    Thread.sleep(5000)

    val key = km.request(target = "testTarget", numberOfCalls = 8)

    assert(key.isDefined)
    assert(key.get == "testKey")
  }

  test("Refreshing should reset number of calls to limit") {
    km.set("testTarget", "testKey", 15, 1000)
    km.request("testTarget", 10)

    Thread.sleep(5000)

    // Refresh is only needed after new key
    km.request("testTarget", 1)

    val num = km.get("testTarget", "testKey")
    assert(num.get == (15 - 1))
  }

  test("RedisKeyManager has a valid default root") {
    val km = new RedisKeyManager("redis://localhost:6379")
    val key = km.request("randomTarget", 1)

    assert(key.isEmpty)
  }
}
