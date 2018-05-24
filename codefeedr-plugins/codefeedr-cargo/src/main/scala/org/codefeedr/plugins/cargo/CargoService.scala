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

package org.codefeedr.plugins.cargo


import org.codefeedr.plugins.cargo.CargoProtocol.CrateInfo
import org.json4s.{DefaultFormats}
import org.json4s.jackson.JsonMethods.parse
import scalaj.http.Http

object CargoService {

  val BASE_URL = "https://crates.io/api/v1/crates/"

  implicit val defaultFormats = DefaultFormats

  def crateInfo(crate: String): CrateInfo = cratesRequest(crate)

  def cratesRequest(crate: String): CrateInfo = {
    val request = BASE_URL + crate
    val response = Http(request).
        timeout(connTimeoutMs = 10000, readTimeoutMs = 15000).
        asString

    parseRespose(response.body)
  }

  def parseRespose(crateInfoJSON: String): CrateInfo =
    parse(crateInfoJSON).extract[CrateInfo]


  def extactCrateFromCommitMsg(msg: String) : String =
    msg.split("`")(1).split("#")(0)
}
