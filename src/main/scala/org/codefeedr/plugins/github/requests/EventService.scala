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
package org.codefeedr.plugins.github.requests

import java.util.UUID

import org.codefeedr.plugins.github.GitHubEndpoints
import org.codefeedr.plugins.github.GitHubProtocol.Event

case class Link(page : Int, rel : String)
class EventService {

  var requestHeaders : List[Header] = List()

  def getLatestEvents() : List[Event] = {
    var lastPage = 10
    var currentPage = 1

    var eventsSize = 0

    while (currentPage < lastPage) {
      println("Now retrieving page " + currentPage)
      val firstResponse = new GitHubRequest(GitHubEndpoints.EVENTS + s"?page=$currentPage", requestHeaders).request()

      println(firstResponse.body)
      println(firstResponse.status)
      firstResponse.headers.foreach(x => println(s"${x.key}: ${x.value.mkString(",")}"))
      updateRequestHeaders(firstResponse.headers)

      eventsSize += 30

      val links = parseLink(firstResponse.headers.find(_.key == "Link").get.value(0))
      currentPage = links.find(_.rel == "next").get.page
      lastPage = links.find(_.rel == "last").get.page
    }

    println(eventsSize)

    //val secondResponse = new GitHubRequest(GitHubEndpoints.EVENTS, requestHeaders).request()
    //println(secondResponse.body)
    //println(secondResponse.status)
    //secondResponse.headers.foreach(x => println(s"${x.key}: ${x.value.mkString(",")}"))

    List()
  }

  def parseLink(url: String): List[Link] = {
    url
      .split(",")
      .map { x =>
        val digitRegex = "(\\d+)".r
        val wordRegex = "\"(\\w+)\"".r
        Link(digitRegex.findFirstIn(x).get.toInt, wordRegex.findFirstIn(x).get.replace("\"", ""))
      }
      .toList
  }

  def updateRequestHeaders(newHeaders : List[Header]) =  {
    if (newHeaders.exists(_.key == "ETag")) {
      updateOrAddHeader("If-None-Match", newHeaders.filter(_.key == "ETag").head.value)
    }
  }

  def updateOrAddHeader(header : Header) : Unit = updateOrAddHeader(header.key, header.value)

  def updateOrAddHeader(key: String, value: Array[String]) = {
    if (requestHeaders.exists(_.key == key)) {
      requestHeaders = requestHeaders.filter(_.key != key)
    }

    requestHeaders = Header(key, value) :: requestHeaders
  }

  def setKey(apiKey: String) = {
    updateOrAddHeader("Authorization", Array(s"token $apiKey"))
  }
}
