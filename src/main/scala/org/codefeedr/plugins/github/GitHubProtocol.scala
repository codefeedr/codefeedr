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
package org.codefeedr.plugins.github

import org.codefeedr.pipeline.PipelineItem
import org.json4s.JObject

object GitHubProtocol {

  /**
    * START /events
    */
  case class Event(id: String,
                   `type`: String,
                   actor: Actor,
                   repo: Repo,
                   organization: Option[Organization],
                   payload: String,
                   public: Boolean,
                   created_at: String) extends PipelineItem

  case class Actor(id: Long,
                   login: String,
                   display_login: String,
                   gravatar_id: String,
                   url: String,
                   avatar_url: String)

  case class Repo(id: Long, name: String, url: String)

  case class Organization(id: Long, login: String)

  sealed abstract class Payload()

  /**
    * END /events
    */

  /**
    * START PushEvents
    */
  case class PushPayload(push_id: Long,
                         size: Int,
                         distinct_size: Int,
                         ref: String,
                         head: String,
                         before: String,
                         commits: List[PushCommit]) extends Payload

  case class PushCommit(sha: String, author: PushAuthor, message: String, distinct: Boolean)

  case class PushAuthor(email: String, name: String)

  /**
    * END PushEvents
    */


}
