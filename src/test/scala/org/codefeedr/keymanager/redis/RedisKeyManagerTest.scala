package org.codefeedr.keymanager.redis

import org.scalatest.{BeforeAndAfter, FunSuite, PrivateMethodTester}

class RedisKeyManagerTest extends FunSuite
  with BeforeAndAfter
  with PrivateMethodTester {

  var km: RedisKeyManager = _

  private val disconnectFn = PrivateMethod[Unit]('disconnect)
  private val setFn = PrivateMethod[Unit]('set)
  private val getFn = PrivateMethod[Option[Int]]('get)
  private val deleteFn = PrivateMethod[Unit]('delete)
  private val deleteAllFn = PrivateMethod[Unit]('deleteAll)

  before {
    km = new RedisKeyManager("redis://localhost:6379", "cf_test")
  }

  after {
    km invokePrivate deleteAllFn()
    km invokePrivate disconnectFn()
  }

  test("A set key should be retrievable" ) {
    km invokePrivate setFn("testTarget", "testKey", 10)

    val key = km.request("testTarget", 1)

    assert(key.isDefined)
    assert(key.get == "testKey")
  }

  test("The key with the best fitting number of calls should be used" ) {
    km invokePrivate setFn("testTarget", "testKey", 10)
    km invokePrivate setFn("testTarget", "testKey2", 4)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get == "testKey2")
  }

  test("After getting a key the number of calls remaining should be lowered") {
    km invokePrivate setFn("testTarget", "testKey", 10)

    val key = km.request("testTarget", 3)

    assert(key.isDefined)
    assert(key.get == "testKey")

    val remaining = km invokePrivate[Option[Int]] getFn("testTarget", "testKey")

    assert(remaining.isDefined)
    assert(remaining.get == (10 - 3))
  }
}
