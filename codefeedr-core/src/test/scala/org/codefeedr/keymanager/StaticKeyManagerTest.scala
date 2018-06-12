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