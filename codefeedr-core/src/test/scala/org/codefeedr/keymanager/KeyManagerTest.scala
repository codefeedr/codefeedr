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
package org.codefeedr.keymanager

import org.scalatest.FunSuite

class KeyManagerTest extends FunSuite {

  var keyManager : KeyManager = null

  // Inject a keymanager into this test suite
  def injectKeyManager(keyManager : KeyManager): Unit = {
    this.keyManager = keyManager
  }

  test("An unknown target returns no keys") {
    val key = keyManager.request("otherTarget", 1)
    assert(key.isEmpty)
  }

  test("An unknown target returns no keys with convenience function") {
    val key = keyManager.request("otherTarget")
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
