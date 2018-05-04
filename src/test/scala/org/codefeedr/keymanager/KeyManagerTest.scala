package org.codefeedr.keymanager

import org.scalatest.FunSuite

class KeyManagerTest(keyManager: KeyManager) extends FunSuite {

  test("An unknown target returns no keys") {
    val key = keyManager.request("otherTarget", 1)
    assert(key.isEmpty)
  }

  test("When key will not be used, no key is returned") {
    assert(keyManager.request("target", 0).isEmpty)
  }

  test("When requesting a key the target must not be null") {
    assertThrows[IllegalArgumentException] {
      keyManager.request(null, 1)
    }
  }

}
