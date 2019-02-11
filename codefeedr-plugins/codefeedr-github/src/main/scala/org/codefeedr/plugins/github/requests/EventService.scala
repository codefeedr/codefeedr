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

import org.codefeedr.keymanager.{KeyManager, ManagedKey}
import org.codefeedr.plugins.github.GitHubEndpoints
import org.codefeedr.plugins.github.GitHubProtocol.Event
import org.codefeedr.stages.utilities.DuplicateService
import org.json4s._
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods._

import scala.util.control.Breaks._

/**
  * Represents a link header from the GitHub API.
  * This shows the previous/next/last page you can retrieve from.
  *
  * @param page the page id.
  * @param rel  the previous/next/last page.
  */
case class Page(page: Int, rel: String)

/**
  * GitHub events service.
  * @param duplicateFilter if there should be checked for duplicates.
  * @param keyManager the keymanager to use for the requests.
  * @param duplicateCheckSize
  */
class EventService(duplicateFilter: Boolean,
                   keyManager: KeyManager,
                   duplicateCheckSize: Int = 1000000) {

  //events size
  val EVENTS_SIZE = 100

  //duplicate filter
  val dupCheck = new DuplicateService[String](duplicateCheckSize)

  var requestHeaders: List[Header] = List()

  /**
    * Requests the latest events.
    * Most often there are 3 pages with events, so 3 requests.
    * @return a list of events.
    */
  def getLatestEvents(): List[Event] = {
    var lastPage = Int.MaxValue
    var nextPage = 1

    var events: List[Event] = List()
    var status = 200

    breakable {
      while (status == 200 && nextPage <= lastPage) {
        //before each request, request a key
        if (keyManager != null) {
          setKey(
            keyManager
              .request("events_source")
              .getOrElse(ManagedKey("", 0))
              .value)
        }

        //do the request
        val response = doPagedRequest(
          s"${GitHubEndpoints.EVENTS}?${GitHubEndpoints.EVENTS_SIZE_SEGMENT}$EVENTS_SIZE&${GitHubEndpoints.EVENTS_PAGE_SEGMENT}$nextPage")

        //update status and new request headers
        status = response.status
        updateRequestHeaders(response.headers)

        //add new events
        val newEvents = parseEvents(response.body)
        events = (if (duplicateFilter)
                    dupCheck.deduplicate[Event](newEvents, _.id)
                  else newEvents) ::: events

        if (nextPage == lastPage) break

        //update pages to keep retrieving the events
        val pages =
          parseNextAndLastPage(response.headers.find(_.key == "Link").get)
        nextPage = pages._1
        lastPage = pages._2
      }
    }

    events
  }

  /**
    * Parse all JSON events into an Event case class.
    * @param body the body to parse.
    * @return the list of events.
    */
  def parseEvents(body: String): List[Event] = {
    implicit val defaultFormats = DefaultFormats ++ JavaTimeSerializers.all

    //if there is no body just return an empty list
    if (body == "") return List()

    val json = parse(body)
    json
      .transformField {
        case JField("payload", list: JObject) =>
          ("payload", JString(compact(render(list))))
        case JField("type", JString(x)) if x.endsWith("Event") =>
          JField("eventType", JString(x)) //transform type field
      }
      .extract[List[Event]]
  }

  /**
    * Parses the next and last page based on the link header.
    *
    * @param linkHeader the lnkheader to parse from.
    * @return a tuple containing the next and last page id.
    */
  def parseNextAndLastPage(linkHeader: Header): (Int, Int) = {
    val pages = parsePages(linkHeader.value(0))

    //get current and last page
    val nextPage = pages
      .find(_.rel == "next")
      .get
      .page
    val lastPage = pages
      .find(_.rel == "last")
      .get
      .page

    (nextPage, lastPage)
  }

  /**
    * Do a request for a certain page.
    *
    * @param endPoint the page endpoint
    * @return a github response.
    */
  def doPagedRequest(endPoint: String): GitHubResponse = {
    new GitHubRequest(endPoint, requestHeaders)
      .request()
  }

  /**
    * Parses the link header into 'pages'.
    *
    * @param url the full header value.
    * @return a list of pages.
    */
  def parsePages(url: String): List[Page] = {
    url
      .split(",")
      .map { x =>
        val digitRegex = "&page=(\\d+)".r
        val wordRegex = "\"(\\w+)\"".r
        Page(digitRegex.findFirstIn(x).get.replace("&page=", "").toInt,
             wordRegex.findFirstIn(x).get.replace("\"", ""))
      }
      .toList
  }

  /**
    * Update all the request headers based on the response headers.
    *
    * @param reponseHeaders the response headers.
    */
  def updateRequestHeaders(reponseHeaders: List[Header]) = {
    if (reponseHeaders.exists(_.key == "ETag")) {
      updateOrAddHeader("If-None-Match",
                        reponseHeaders.filter(_.key == "ETag").head.value)
    }
  }

  /**
    * Updates or adds a header.
    *
    * @param header the header to add or update.
    */
  def updateOrAddHeader(header: Header): Unit =
    updateOrAddHeader(header.key, header.value)

  /**
    * Updates or adds a request header.
    *
    * @param key   the header key.
    * @param value the header value.
    */
  def updateOrAddHeader(key: String, value: Array[String]) = {
    if (requestHeaders.exists(_.key == key)) {
      requestHeaders = requestHeaders.filter(_.key != key)
    }

    requestHeaders = Header(key, value) :: requestHeaders
  }

  /**
    * Set the API key of the event-service.
    *
    * @param apiKey the API key to use.
    */
  def setKey(apiKey: String) = {
    updateOrAddHeader("Authorization", Array(s"token $apiKey"))
  }

}
