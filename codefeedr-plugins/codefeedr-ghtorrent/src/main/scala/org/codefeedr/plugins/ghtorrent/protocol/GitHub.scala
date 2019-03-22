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
  /**
    * START Delete
    */
  case class DeleteEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: DeletePayload,
                         public: Boolean,
                         created_at: Date)
      extends Event

  case class DeletePayload(ref: String, ref_type: String, pusher_type: String)

  /**
    * END Delete
    */
  /**
    * START CommitComment
    */
  case class CommitCommentEvent(id: String,
                                _id: _id,
                                `type`: String,
                                actor: Actor,
                                repo: Repo,
                                organization: Option[Organization],
                                payload: CommitCommentPayload,
                                public: Boolean,
                                created_at: Date)
      extends Event

  case class CommitCommentPayload(comment: Comment)

  case class Comment(url: String,
                     html_url: String,
                     id: Long,
                     node_id: String,
                     user: User,
                     position: Option[Int],
                     line: Option[Int],
                     path: Option[String],
                     commit_id: String,
                     created_at: Date,
                     updated_at: Date,
                     author_association: String,
                     body: String)

  /**
    * END CommitComment
    */
  /**
    * START PullRequest
    */
  case class PullRequestEvent(id: String,
                              _id: _id,
                              `type`: String,
                              actor: Actor,
                              repo: Repo,
                              organization: Option[Organization],
                              payload: PullRequestPayload,
                              public: Boolean,
                              created_at: Date)
      extends Event

  case class PullRequestPayload(action: String,
                                number: Int,
                                pull_request: PullRequest)

  case class PullRequest(url: String,
                         id: String,
                         node_id: String,
                         number: Int,
                         state: String,
                         locked: Boolean,
                         title: String,
                         user: User,
                         body: String,
                         created_at: Date,
                         updated_at: Date,
                         closed_at: Date,
                         merged_at: Date,
                         merge_commit_sha: String,
                         assignee: Option[User],
                         assignees: List[User],
                         requested_reviewers: List[User],
                         requested_teams: List[Team],
                         head: PullRequestMarker,
                         base: PullRequestMarker,
                         labels: List[Label],
                         milestone: Option[Milestone],
                         author_association: String,
                         merged: Boolean,
                         mergeable: Option[Boolean],
                         rebaseable: Option[Boolean],
                         mergeable_state: String,
                         merged_by: Option[User],
                         comments: Int,
                         review_comments: Int,
                         maintainer_can_modify: Boolean,
                         commits: Int,
                         additions: Double,
                         deletions: Double,
                         changed_files: Int)

  case class Team(id: Long,
                  node_id: String,
                  url: String,
                  name: String,
                  slug: String,
                  description: String,
                  privacy: String,
                  permission: String,
                  members_url: String,
                  repositories_url: String,
                  parent: Option[Team])

  case class Milestone(url: String,
                       html_url: String,
                       labels_url: String,
                       id: Long,
                       node_id: String,
                       number: Int,
                       state: String,
                       title: String,
                       description: String,
                       creator: User,
                       open_issues: Int,
                       closed_issues: Int,
                       created_at: Date,
                       updated_at: Date,
                       due_on: Date)

  case class PullRequestMarker(label: String,
                               ref: String,
                               sha: String,
                               user: User,
                               repo: Repository)

  case class Repository(id: Long,
                        name: String,
                        node_id: String,
                        full_name: String,
                        `private`: Boolean,
                        owner: User,
                        description: String,
                        fork: Boolean,
                        created_at: Date,
                        updated_at: Date,
                        pushed_at: Date,
                        homepage: String,
                        size: Double,
                        stargazers_count: Double,
                        watchers_count: Double,
                        language: String,
                        has_issues: Boolean,
                        has_projects: Boolean,
                        has_downloads: Boolean,
                        has_wiki: Boolean,
                        has_pages: Boolean,
                        archived: Boolean,
                        open_issues_count: Double,
                        license: License,
                        forks: Double,
                        open_issues: Double,
                        watchers: Double,
                        default_branch: String)

  case class License(key: String,
                     name: String,
                     spdx_id: String,
                     url: String,
                     node_id: String)

  case class Label(id: Long,
                   node_id: String,
                   url: String,
                   name: String,
                   description: String,
                   color: String,
                   default: Boolean)

  /**
    * END PullRequest
    */
  /**
    * START Deployment
    */
  case class DeploymentEvent(id: String,
                             _id: _id,
                             `type`: String,
                             actor: Actor,
                             repo: Repo,
                             organization: Option[Organization],
                             payload: DeploymentPayload,
                             public: Boolean,
                             created_at: Date)
      extends Event

  case class DeploymentPayload(deployment: Deployment,
                               repository: Repository,
                               sender: Option[User])

  case class Deployment(url: String,
                        id: Long,
                        node_id: String,
                        sha: String,
                        ref: String,
                        task: String,
                        environment: String,
                        creator: User,
                        created_at: Date,
                        updated_at: Date,
                        statuses_url: String,
                        repository_url: String)

  /**
    * END Deployment
    */
  /**
    * START DeploymentStatus
    */
  case class DeploymentStatusEvent(id: String,
                                   _id: _id,
                                   `type`: String,
                                   actor: Actor,
                                   repo: Repo,
                                   organization: Option[Organization],
                                   payload: DeploymentStatusPayload,
                                   public: Boolean,
                                   created_at: Date)
      extends Event

  case class DeploymentStatusPayload(deployment_status: DeploymentStatus,
                                     deployment: Deployment,
                                     repository: Repository,
                                     sender: Option[User])

  case class DeploymentStatus(url: String,
                              id: Long,
                              node_id: String,
                              state: String,
                              creator: User,
                              description: String,
                              target_url: String,
                              created_at: Date,
                              updated_at: Date,
                              deployment_url: String,
                              repository_url: String)

  /**
    * END DeploymentStatus
    */
  /**
    * START ForkEvent
    */
  case class ForkEvent(id: String,
                       _id: _id,
                       `type`: String,
                       actor: Actor,
                       repo: Repo,
                       organization: Option[Organization],
                       payload: ForkPayload,
                       public: Boolean,
                       created_at: Date)
      extends Event

  case class ForkPayload(forkee: Repository)

  /**
    * END ForkEvent
    */
  /**
    * START GollumEvent
    */
  case class GollumEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: GollumPayload,
                         public: Boolean,
                         created_at: Date)
      extends Event

  case class GollumPayload(pages: List[Page])

  case class Page(page_name: String,
                  title: String,
                  summary: Option[String],
                  action: String,
                  sha: String,
                  html_url: String)

  /**
    * END GollumEvent
    */
  /**
    * START IssuesEvent
    */
  case class IssuesEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: IssuePayload,
                         public: Boolean,
                         created_at: Date)
      extends Event

  case class IssuePayload(action: String, issue: Issue)

  case class Issue(url: String,
                   id: Long,
                   node_id: String,
                   number: Double,
                   title: String,
                   user: User,
                   labels: List[Label],
                   state: String,
                   locked: Boolean,
                   assignee: Option[User],
                   assignees: List[User],
                   milestone: Option[Milestone],
                   comments: Double,
                   created_at: Date,
                   updated_at: Date,
                   closed_at: Option[Date],
                   author_association: String,
                   body: String)

  /**
  * END IssuesEvent
  */
}
