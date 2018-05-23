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

package org.codefeedr.pipeline

import org.codefeedr.testUtils._
import org.scalatest.FunSuite

class PipelineObjectListTest extends FunSuite {

  val a = new SimpleSourcePipelineObject()
  val b = new SimpleTransformPipelineObject()
  val c = new SimpleSinkPipelineObject()

  test("Adding an item should append to the end") {
    val list = new PipelineObjectList().add(a).add(b).add(c)

    assert(list.head == a)
    assert(list.tail.head == b)
    assert(list.last == c)

    assert(list.size == 3)
    assert(!list.isEmpty)
    assert(list.nonEmpty)
  }

  test("Foreach should be ordered correctly") {
    var i = 0
    val list = new PipelineObjectList().add(a).add(b).add(c)

    for (item <- list) {
      if (i == 0) {
        assert(item == a)
      } else if (i == 1) {
        assert(item == b)
      } else if (i == 2) {
        assert(item == c)
      }

      i = i + 1
    }
  }

  test("Appending items with append operator is equivalent to adding") {
    val list = new PipelineObjectList().add(a).add(b)
    val list2 = new PipelineObjectList() :+ a :+ b

    assert(list == list2)
  }

  test("Concatenating objects is equivalent to making a list and adding") {
    val list = new PipelineObjectList() :+ a :+ b
    val list2 = a :+ b

    assert(list == list2)
  }

  test("Descriptor is same as list") {
    val list = new PipelineObjectList()

    assert(list.toString == List().toString())
  }

  test("Equivalency operator works") {
    val list = new PipelineObjectList()

    assert(list != "a")
  }

  test("Can append two lists") {
    val list1 = new PipelineObjectList() :+ a
    val list2 = new PipelineObjectList()
    val list3 = new PipelineObjectList() :+ b

    assert(list1 :+ list2 == list1)
    assert(list1 :+ list3 == a :+ b)
  }
}
