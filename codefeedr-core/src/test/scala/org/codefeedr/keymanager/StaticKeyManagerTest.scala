package org.codefeedr.keymanager

import org.scalatest.FunSuite

class StaticKeyManagerTest extends KeyManagerTest(new StaticKeyManager()) {

  test("An empty key manager returns no keys") {
    val km = new StaticKeyManager()

    val key = km.request("any", 1)
    assert(key.isEmpty)
  }

  test("Supplying no map of keys creates an empty key manager") {
    val km = new StaticKeyManager()

    assert(km.request("target", 1).isEmpty)
  }

  test("The same key as set is returned for the same target") {
    val km = new StaticKeyManager(Map("aTarget" -> "aKey", "bTarget" -> "bKey"))

    assert(km.request("aTarget", 1).get.value == "aKey")
    assert(km.request("bTarget", 1).get.value == "bKey")
  }
}