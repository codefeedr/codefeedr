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
package org.codefeedr.keymanager

/**
  * A key managed by a key manager.
  *
  * @param value The key
  * @param remainingCalls Number of calls remaining after using the ones requested. Can be 0.
  */
case class ManagedKey(value: String, remainingCalls: Int)

/**
  * A key manager handles the retrieval of API keys for use with, e.g. web APIs.
  *
  * The key manager has multiple targets, each with a set of keys. A key can have
  * a limited amount of calls it can be used for. The key manager handles refreshing
  * of those number of calls.
  */
trait KeyManager {

  /**
    * Request a key for given target with at least the number of calls given remaining.
    *
    * The number of calls will be used to count how many are still remaining.
    *
    * @param target Target of the key,
    * @param numberOfCalls The number of calls needed on this key.
    * @return Managed key
    */
  def request(target: String, numberOfCalls: Int): Option[ManagedKey]

  /**
    * Request single-use key for given target.
    *
    * Convenience method.
    *
    * @param target Target of the key
    * @return Managed key
    */
  def request(target: String): Option[ManagedKey] = request(target, 1)
}
