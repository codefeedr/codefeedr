---
title: Frequently Asked Questions
permalink: mydoc_codefeedr_faq.html
sidebar: mydoc_sidebar
tags: [troubleshooting]
toc: false
keywords: frequently asked questions, FAQ, question and answer, collapsible sections, expand, collapse
folder: mydoc
---

<p></p>

<div class="panel-group" id="accordion">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            <h4 class="panel-title">
                                <a class="noCrossRef accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#collapseOne">I'm getting an error related to missing an implicit evidence parameter type.</a>
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

{% include links.html %}
