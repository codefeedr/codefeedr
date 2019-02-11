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

/**
  * A list of pipeline object.
  *
  * A List can't hold POs because of the typing system. This circumvents that.
  *
  * @param list
  */
class PipelineObjectList(private val list: List[AnyRef] = List())
    extends Serializable {

  /**
    * Add an object to the list. Returns a new list.
    *
    * @param obj Object to add
    * @return List with the object included at the end
    */
  def add[U <: Serializable with AnyRef, V <: Serializable with AnyRef](
      obj: PipelineObject[U, V]): PipelineObjectList =
    new PipelineObjectList(list :+ obj)

  /**
    * Selects the first element of this List.
    *
    * @return  the first element of this list.
    * @throws NoSuchElementException if the list is empty.
    */
  def head: PipelineObject[Serializable with AnyRef, Serializable with AnyRef] =
    list.head.asInstanceOf[PipelineObject[Serializable with AnyRef,
                                          Serializable with AnyRef]]

  /**
    * Selects the last element.
    *
    * @return The last element of this list.
    * @throws NoSuchElementException If the list is empty.
    */
  def last: PipelineObject[Serializable with AnyRef, Serializable with AnyRef] =
    list.last.asInstanceOf[PipelineObject[Serializable with AnyRef,
                                          Serializable with AnyRef]]

  /**
    * Selects all elements except the first.
    *
    * @return  a list consisting of all elements of this list
    *          except the first one.
    * @throws UnsupportedOperationException if the list is empty.
    */
  def tail: PipelineObjectList = new PipelineObjectList(list.tail)

  /**
    * Get the size of the list
    * @return Size
    */
  def size: Int = list.size

  /**
    * Get whether the list is empty
    * @return True when empty
    */
  def isEmpty: Boolean = list.isEmpty

  /**
    * Get whether the list is not empty
    * @return True when not empty
    */
  def nonEmpty: Boolean = list.nonEmpty

  def foreach(f: PipelineObject[Serializable with AnyRef,
                                Serializable with AnyRef] ⇒ Unit): Unit = {
    for (obj <- list) {
      f(
        obj.asInstanceOf[PipelineObject[Serializable with AnyRef,
                                        Serializable with AnyRef]])
    }
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: PipelineObjectList => other.list == this.list
    case _                         => false
  }

  /**
    * Add an element to a copy of the list.
    *
    * @param obj Element
    * @return New list with element appended
    */
  def :+[U <: Serializable with AnyRef, V <: Serializable with AnyRef](
      obj: PipelineObject[U, V]): PipelineObjectList =
    add(obj)

  /**
    * Adds all elements of the given list.
    * @param other List
    * @return New list with elements from other list appended
    */
  def :+(other: PipelineObjectList): PipelineObjectList =
    new PipelineObjectList(list ::: other.list)

  override def toString: String = list.toString()
}
