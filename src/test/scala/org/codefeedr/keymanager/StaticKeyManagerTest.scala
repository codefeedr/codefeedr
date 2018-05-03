package org.codefeedr.keymanager

import org.scalatest.FunSuite

class StaticKeyManagerTest extends FunSuite {

  test("An empty key manager returns no keys") {
    val km = new StaticKeyManager()

    val key = km.request("any", 1)
    assert(key.isEmpty)
  }

  test("An unknown target returns no keys") {
    val km = new StaticKeyManager(Map("target" -> "key"))

    val key = km.request("otherTarget", 1)
    assert(key.isEmpty)
  }

  test("The same key as set is returned for the same target") {
    val km = new StaticKeyManager(Map("aTarget" -> "aKey", "bTarget" -> "bKey"))

    assert(km.request("aTarget", 1).get.value == "aKey")
    assert(km.request("bTarget", 1).get.value == "bKey")
  }

  test("When key will not be used, no key is returned") {
    val km = new StaticKeyManager(Map("target" -> "key"))

    assert(km.request("target", 0).isEmpty)
  }

  test("When requesting a key the target must not be null") {
    val km = new StaticKeyManager()

    assertThrows[IllegalArgumentException] {
      km.request(null, 1)
    }
  }

  test("Supplying no map of keys creates an empty key manager") {
    val km = new StaticKeyManager()

    assert(km.request("target", 1).isEmpty)
  }
}