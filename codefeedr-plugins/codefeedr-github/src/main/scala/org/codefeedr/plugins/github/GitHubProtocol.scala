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

import java.util.Date

object GitHubProtocol {

  /**
    * START /events
    */
  case class Event(id: String,
                   eventType: String,
                   actor: Actor,
                   repo: Repo,
                   organization: Option[Organization],
                   payload: String,
                   public: Boolean,
                   created_at: Date)

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
  case class PushEvent(id: String,
                       eventType: String,
                       actor: Actor,
                       repo: Repo,
                       organization: Option[Organization],
                       payload: PushPayload,
                       public: Boolean,
                       created_at: Date)

  case class PushPayload(push_id: Long,
                         size: Int,
                         distinct_size: Int,
                         ref: String,
                         head: String,
                         before: String,
                         commits: List[PushCommit])
      extends Payload

  case class PushCommit(sha: String,
                        author: PushAuthor,
                        message: String,
                        distinct: Boolean)

  case class PushAuthor(email: String, name: String)

  /**
    * END PushEvents
    */
  /**
    * START IssuesEvents
    */
  case class IssuesEvent(id: String,
                         eventType: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: IssuesPayload,
                         public: Boolean,
                         created_at: Date)

  case class IssuesPayload(action: String, issue: Issue) extends Payload

  case class Issue(url: String,
                   id: Double,
                   number: Double,
                   title: String,
                   user: User,
                   labels: List[Label],
                   state: String,
                   locked: Boolean,
                   assignee: Option[User],
                   assignees: List[User],
                   milestone: Option[String],
                   comments: Double,
                   created_at: Option[Date],
                   updated_at: Option[Date],
                   closed_at: Option[Date],
                   author_association: String,
                   body: Option[String])

  case class Label(id: Long,
                   url: String,
                   name: String,
                   color: String,
                   default: Boolean)

  /**
    * END IssuesEvents
    */
  /**
    * START IssueCommentEvents
    */
  case class IssueCommentEvent(id: String,
                               eventType: String,
                               actor: Actor,
                               repo: Repo,
                               organization: Option[Organization],
                               payload: IssueCommentPayload,
                               public: Boolean,
                               created_at: Date)

  case class IssueCommentPayload(action: String, issue: Issue, comment: Comment)
      extends Payload

  case class Comment(url: String,
                     id: Long,
                     user: User,
                     created_at: Option[Date],
                     updated_at: Option[Date],
                     author_association: String,
                     body: Option[String])

  /**
    * END IssueCommentEvents
    */
  /**
    * START Commit
    */
  case class Commit(sha: String,
                    url: String,
                    commit: CommitData,
                    author: Option[User],
                    committer: Option[User],
                    parents: List[Parent],
                    stats: Stats,
                    files: List[File])

  case class CommitData(author: CommitUser,
                        committer: CommitUser,
                        message: String,
                        tree: Tree,
                        comment_count: Int,
                        verification: Verification)

  case class CommitUser(name: String, email: String, date: String)

  case class User(id: Long,
                  login: String,
                  avatar_url: String,
                  `type`: String,
                  site_admin: Boolean)

  case class Verification(verified: Boolean,
                          reason: String,
                          signature: Option[String],
                          payload: Option[String])

  case class Stats(total: Int, additions: Int, deletions: Int)

  case class File(sha: Option[String],
                  filename: Option[String],
                  status: Option[String],
                  additions: Int,
                  deletions: Int,
                  changes: Int,
                  blob_url: Option[String],
                  raw_url: Option[String],
                  contents_url: Option[String],
                  patch: Option[String])

  case class Parent(sha: String)

  case class Tree(sha: String)

  /**
  * END Commit
  */

}
