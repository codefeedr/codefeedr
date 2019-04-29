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
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#ght">Release extended</a>
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