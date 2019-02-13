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

/** Holds a list of [[Stage]] objects.
  *
  * @param list Initial list.
  */
class StageList(private val list: List[AnyRef] = List()) extends Serializable {

  /** Add an object to the StageList. Returns a new list.
    *
    * @param stage Stage to add.
    * @return StageList with the stage included at the end.
    */
  def add[U <: Serializable with AnyRef, V <: Serializable with AnyRef](
      stage: Stage[U, V]): StageList =
    new StageList(list :+ stage)

  /** Selects the first element of this StageList.
    *
    * @return The first element of this list.
    * @throws NoSuchElementException When the list is empty.
    */
  def head: Stage[Serializable with AnyRef, Serializable with AnyRef] =
    list.head
      .asInstanceOf[Stage[Serializable with AnyRef, Serializable with AnyRef]]

  /** Select the last element of this StageList.
    *
    * @return The last element of this list.
    * @throws NoSuchElementException When the list is empty.
    */
  def last: Stage[Serializable with AnyRef, Serializable with AnyRef] =
    list.last
      .asInstanceOf[Stage[Serializable with AnyRef, Serializable with AnyRef]]

  /** Selects all elements except the first.
    *
    * @return  A StageList consisting of all elements of this list
    *          except the first one.
    * @throws UnsupportedOperationException When the list is empty.
    */
  def tail: StageList = new StageList(list.tail)

  /** Get the size of the list.
    *
    * @return Size of the list.
    */
  def size: Int = list.size

  /** Check whether the list is empty.
    *
    * @return True when list is empty.
    */
  def isEmpty: Boolean = list.isEmpty

  /** Check whether the list is non empty.
    *
    * @return True when list is not empty.
    */
  def nonEmpty: Boolean = list.nonEmpty

  /** Iterates through all stages and applies a function.
    *
    * @param f Function to apply to each stage.
    */
  def foreach(
      f: Stage[Serializable with AnyRef, Serializable with AnyRef] ⇒ Unit)
    : Unit = {
    for (obj <- list) {
      f(
        obj.asInstanceOf[Stage[Serializable with AnyRef,
                               Serializable with AnyRef]])
    }
  }

  /** StageList equality.
    *
    * @param obj Object to compare to.
    * @return True if this and obj are equal.
    */
  override def equals(obj: scala.Any): Boolean = obj match {
    case other: StageList => other.list == this.list
    case _                => false
  }

  /** Adds an element to a copy of the list.
    *
    * @param stage The stage to add.
    * @return New StageList with element appended.
    */
  def :+[U <: Serializable with AnyRef, V <: Serializable with AnyRef](
      stage: Stage[U, V]): StageList =
    add(stage)

  /** Adds all elements of the given list.
    *
    * @param other The other StageList.
    * @return New list with elements from other list appended.
    */
  def :+(other: StageList): StageList =
    new StageList(list ::: other.list)

  /** Stringify the StageList. */
  override def toString: String = list.toString()
}
