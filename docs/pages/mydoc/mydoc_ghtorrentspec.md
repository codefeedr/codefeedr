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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#event">Event related</a>
                            </h4>
                        </div>
                        <div id="event" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
case class Actor(id: Long,
               login: String,
               display_login: String,
               gravatar_id: String,
               url: String,
               avatar_url: String)

case class Repo(id: Long, name: String, url: String)

case class Organization(id: Long, login: String)
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#ght">GHTorrent related</a>
                            </h4>
                        </div>
                        <div id="ght" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
case class Record(routingKey: String, contents: String)

case class _id(`$oid`: String)
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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#issues">Issues</a>
                            </h4>
                        </div>
                        <div id="issues" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
case class IssuesEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: IssuesPayload,
                         public: Boolean,
                         created_at: Date)
      extends Event


case class IssuesPayload(action: String, issue: Issue)

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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#cc">CommitComment</a>
                            </h4>
                        </div>
                        <div id="cc" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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

case class CommitCommentPayload(comment: CommitComment)

case class CommitComment(url: String,
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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#create">Create</a>
                            </h4>
                        </div>
                        <div id="create" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#delete">Delete</a>
                            </h4>
                        </div>
                        <div id="delete" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#deployment">Deployment</a>
                            </h4>
                        </div>
                        <div id="deployment" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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

case class DeploymentPayload(deployment: Deployment)

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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#deploystatus">DeploymentStatus</a>
                            </h4>
                        </div>
                        <div id="deploystatus" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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
                                 deployment: Deployment)

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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#fork">Fork</a>
                            </h4>
                        </div>
                        <div id="fork" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#gollum">Gollum</a>
                            </h4>
                        </div>
                        <div id="gollum" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#issuecom">IssueComment</a>
                            </h4>
                        </div>
                        <div id="issuecom" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
case class IssueCommentEvent(id: String,
                           _id: _id,
                           `type`: String,
                           actor: Actor,
                           repo: Repo,
                           organization: Option[Organization],
                           payload: IssueCommentPayload,
                           public: Boolean,
                           created_at: Date)
  extends Event

case class IssueCommentPayload(action: String,
                             issue: Issue,
                             comment: IssueComment)

case class IssueComment(url: String,
                      node_id: String,
                      id: Double,
                      user: User,
                      created_at: Date,
                      updated_at: Date,
                      author_association: String,
                      body: String)

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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#member">Member</a>
                            </h4>
                        </div>
                        <div id="member" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
case class MemberEvent(id: String,
                     _id: _id,
                     `type`: String,
                     actor: Actor,
                     repo: Repo,
                     organization: Option[Organization],
                     payload: MemberPayload,
                     public: Boolean,
                     created_at: Date)
  extends Event

case class MemberPayload(member: User, action: String)

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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#membership">Membership</a>
                            </h4>
                        </div>
                        <div id="membership" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
case class MemberShipEvent(id: String,
                         _id: _id,
                         `type`: String,
                         actor: Actor,
                         repo: Repo,
                         organization: Option[Organization],
                         payload: MemberShipPayload,
                         public: Boolean,
                         created_at: Date)
  extends Event

case class MemberShipPayload(action: String,
                           scope: String,
                           member: User,
                           team: Team,
                           org: Option[Organization])
                           
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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#pagebuild" href="#pagebuild">PageBuild</a>
                            </h4>
                        </div>
                        <div id="pagebuild" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
case class PageBuildEvent(id: String,
                        _id: _id,
                        `type`: String,
                        actor: Actor,
                        repo: Repo,
                        organization: Option[Organization],
                        payload: PageBuildPayload,
                        public: Boolean,
                        created_at: Date)
  extends Event

case class PageBuildPayload(id: Long, build: PageBuild)

case class PageBuild(url: String,
                   status: String,
                   error: Error,
                   pusher: User,
                   commit: String,
                   duration: Long,
                   created_at: Date,
                   updated_at: Date)

case class Error(message: Option[String])

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
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#public">Public</a>
                            </h4>
                        </div>
                        <div id="public" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body"> 
{% highlight scala %}
case class PublicEvent(id: String,
                     _id: _id,
                     `type`: String,
                     actor: Actor,
                     repo: Repo,
                     organization: Option[Organization],
                     public: Boolean,
                     created_at: Date)
  extends Event
{% endhighlight %}                       
                               </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->