---
title: "GHTorrent specification"
keywords: plugins, ghtorrent
tags: [plugins]
sidebar: mydoc_sidebar
highlighter: rouge
permalink: mydoc_ghtorrentspec.html
---

This page gives the specification of all data types used in the GHTorrent plugin. You can re-use these classes by [adding the GHTorrent plugin as dependency](/mydoc_ghtorrent.html#installation). 

<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#commit">Commit</a>
                            </h4>
                        </div>
                        <div id="commit" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
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

  case class CommitUser(name: String, email: String, date: Date)

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
{% endhighlight %}
                            </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->

<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#pr">PullRequest</a>
                            </h4>
                        </div>
                        <div id="pr" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
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
                         closed_at: Option[Date],
                         merged_at: Option[Date],
                         merge_commit_sha: Option[String],
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
                       description: Option[String],
                       creator: Option[User],
                       open_issues: Int,
                       closed_issues: Int,
                       created_at: Date,
                       updated_at: Option[Date],
                       due_on: Option[Date],
                       closed_at: Option[Date])

  case class PullRequestMarker(label: String,
                               ref: String,
                               sha: String,
                               user: User,
                               repo: Option[Repository])

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
                        homepage: Option[String],
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
                        license: Option[License],
                        forks: Double,
                        open_issues: Double,
                        watchers: Double,
                        default_branch: String)

  case class License(key: String,
                     name: String,
                     spdx_id: String,
                     url: Option[String],
                     node_id: String)

  case class Label(id: Long,
                   node_id: String,
                   url: String,
                   name: String,
                   color: String,
                   default: Boolean)
  
  case class User(id: Long,
                    login: String,
                    avatar_url: String,
                    `type`: String,
                    site_admin: Boolean)               

{% endhighlight %}                              
                               </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->