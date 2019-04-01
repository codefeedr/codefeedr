---
title: "GHTorrent specification"
keywords: plugins, ghtorrent
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_ghtorrentspec.html
---

This page gives the specification of all data types used in the GHTorrent plugin. You can re-use these classes by [adding the GHTorrent plugin as dependency](/mydoc_ghtorrent.html#installation). 

<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">Commit</a>
                            </h4>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse noCrossRef">
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">PullRequest</a>
                            </h4>
                        </div>
                        <div id="collapseOne" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
                                Make sure to import the TypeInformation implicits from Flink: <code>import org.apache.flink.streaming.api.scala._</code>. Also see <a href="https://flink.apache.org/gettinghelp.html#got-an-error-message">this</a>.
                            </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->