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
package org.codefeedr.stages.utilities

/**
  *
  * @param queueSize
  * @tparam T
  */
class DuplicateService[T](queueSize: Int) {

  val queue: FiniteQueue[T] = new FiniteQueue[T]()

  /**
    * Check and removes duplicates based on the contents of a queue.
    * @param items the items to check for duplicates.
    * @param getField function which retrieves a field based on the type to check.
    * @tparam IN the type on which the duplicate check should be done.
    * @return a list of IN without any duplicates based on a queue.
    */
  def deduplicate[IN](items: List[IN], getField: (IN) => T): List[IN] = {
    items.distinct
      .filter(x => !queue.contains(getField(x)))
      .map { x =>
        queue.enqueueFinite(getField(x), queueSize)
        x
      }
  }

  /**
    * Check and removes duplicates based on the contents of a queue.
    * @param items the items to check for duplicates.
    * @return  a list of T without any duplicates based on a queue.
    */
  def deduplicate(items: List[T]): List[T] = {
    deduplicate[T](items, x => x)
  }



}
