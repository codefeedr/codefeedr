package org.codefeedr.plugins.ghtorrent.protocol

import java.util.Date

import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.{Event, _id}

object GitHub {

  /**
    * START Commit
    */
  case class Commit(_id: _id,
                    node_id: String,
                    sha: String,
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
  /**
    * START Event
    */
  case class Actor(id: Long,
                   login: String,
                   display_login: String,
                   gravatar_id: String,
                   url: String,
                   avatar_url: String)

  case class Repo(id: Long, name: String, url: String)

  case class Organization(id: Long, login: String)

  /**
    * END Event
    */
  /**
    * START PushEvent
    */
  case class PushEvent(id: String,
                       _id: _id,
                       `type`: String,
                       actor: Actor,
                       repo: Repo,
                       organization: Option[Organization],
                       payload: PushPayload,
                       public: Boolean,
                       created_at: Date)
      extends Event

  case class PushPayload(push_id: Long,
                         size: Int,
                         distinct_size: Int,
                         ref: String,
                         head: String,
                         before: String,
                         commits: List[PushCommit])

  case class PushCommit(sha: String,
                        author: PushAuthor,
                        message: String,
                        distinct: Boolean)

  case class PushAuthor(email: String, name: String)

  /**
    * END PushEvent
    */
  /**
    * START Create
    */
  case class CreateEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: CreatePayload,
                         public: Boolean,
                         created_at: Date)
      extends Event

  case class CreatePayload(ref: String,
                           ref_type: String,
                           master_branch: String,
                           description: String,
                           pusher_type: String)
  /**
  * END Create
  */
}
