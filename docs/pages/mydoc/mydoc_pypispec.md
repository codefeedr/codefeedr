---
title: "PyPi specification"
keywords: plugins, pypi
tags: [plugins]
sidebar: mydoc_sidebar
highlighter: rouge
toc: false
permalink: mydoc_pypispec.html
---

This page gives the specification of all data types used in the PyPi plugin. You can re-use these classes by [adding the PyPi plugin as dependency](/mydoc_pypi.html#installation). 
<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#event">Release</a>
                            </h4>
                        </div>
                        <div id="event" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
case class PyPiRelease(title: String,
                     link: String,
                     description: String,
                     pubDate: Date)
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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#ght">Release extended</a>
                            </h4>
                        </div>
                        <div id="ght" class="panel-collapse collapse noCrossRef">
                            <div class="panel-body">
{% highlight scala %}
case class PyPiReleaseExt(title: String,
                        link: String,
                        description: String,
                        pubDate: Date,
                        project: PyPiProject)

case class PyPiProject(info: Info,
                     last_serial: Long,
                     releases: List[ReleaseVersion],
                     urls: List[Release])

case class Info(author: String,
              author_email: String,
              bugtrack_url: Option[String],
              classifiers: List[String],
              description: String,
              description_content_type: String,
              docs_url: Option[String],
              download_url: String,
              downloads: Downloads,
              home_page: String,
              keywords: String,
              license: String,
              maintainer: String,
              maintainer_email: String,
              name: String,
              package_url: String,
              platform: String,
              project_url: String,
              project_urls: Option[ProjectUrl],
              release_url: String,
              requires_dist: List[String],
              requires_python: Option[String],
              summary: String,
              version: String)

case class Downloads(last_day: Int, last_month: Int, last_week: Int)

case class ProjectUrl(Homepage: String)

case class ReleaseVersion(version: String, releases: List[Release])

case class Release(comment_text: String,
                 digests: Digest,
                 downloads: Double,
                 filename: String,
                 has_sig: Boolean,
                 md5_digest: String,
                 packagetype: String,
                 python_version: String,
                 requires_python: Option[String],
                 size: Double,
                 upload_time: Date,
                 url: String)

case class Digest(md5: String, sha256: String)
{% endhighlight %}
                            </div>
                        </div>
                    </div>
</div>
<!-- /.panel-group -->