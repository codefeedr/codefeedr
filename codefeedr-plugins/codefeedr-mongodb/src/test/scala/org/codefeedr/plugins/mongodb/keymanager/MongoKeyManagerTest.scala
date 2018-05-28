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

package org.codefeedr.plugins.mongodb.keymanager

import java.util.Date

import org.scalatest.{BeforeAndAfter, FunSuite}

class MongoKeyManagerTest extends FunSuite with BeforeAndAfter {
  var km: MongoKeyManager = _

  before {
    km = new MongoKeyManager()
  }

  after {
    km.clear()
  }

  test("Should return single key in the database") {
    km.add("github", "myKey", 10, 60, new Date())

    val key = km.request("github", 1)

    assert(key.isDefined)
    assert(key.get.value == "myKey")
    assert(key.get.remainingCalls == 9)
  }

  test("Should update number of calls left") {
    km.add("github", "myKey", 10, 60, new Date())

    km.request("github", 1)
    val key = km.request("github", 1)

    assert(key.isDefined)
    assert(key.get.value == "myKey")
    assert(key.get.remainingCalls == 8)
  }


  test("Should give none is there is no key with available number of calls") {
    km.add("github", "myKey", 10, 60, new Date())
    km.add("travis", "otherKey", 10, 60, new Date())

    val key = km.request("github", 11)

    assert(key.isEmpty)
  }

  test("An unknown target returns no keys") {
    val key = km.request("otherTarget", 1)
    assert(key.isEmpty)
  }

  test("An unknown target returns no keys with convenience function") {
    val key = km.request("otherTarget")
    assert(key.isEmpty)
  }

  test("When key will not be used, no key is returned") {
    assert(km.request("target", 0).isEmpty)
  }

  test("When requesting a key the target must not be null") {
    assertThrows[IllegalArgumentException] {
      km.request(null, 1)
    }
  }
}
