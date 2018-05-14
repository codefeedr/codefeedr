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
class PipelineObjectList(private val list: List[AnyRef] = List()) extends Serializable {

  def add[U <: PipelineItem, V <: PipelineItem](obj: PipelineObject[U, V]): PipelineObjectList =
    new PipelineObjectList(list :+ obj)

  def head: PipelineObject[PipelineItem, PipelineItem] =
    list.head.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]]

  def last: PipelineObject[PipelineItem, PipelineItem] =
    list.last.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]]

  def tail: PipelineObjectList = new PipelineObjectList(list.tail)

  def size: Int = list.size

  def isEmpty: Boolean = list.isEmpty

  def nonEmpty: Boolean = list.nonEmpty

  def foreach(f: PipelineObject[PipelineItem, PipelineItem] ⇒ Unit): Unit = {
    for (obj <- list) {
      f(obj.asInstanceOf[PipelineObject[PipelineItem, PipelineItem]])
    }
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case other: PipelineObjectList => other.list == this.list
    case _ => false
  }

  def :+[U <: PipelineItem, V <: PipelineItem](obj: PipelineObject[U, V]): PipelineObjectList =
    add(obj)

  def :+(other: PipelineObjectList): PipelineObjectList =
    new PipelineObjectList(list ::: other.list)

  override def toString: String = list.toString()
}
