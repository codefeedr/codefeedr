---
title: "GitHub"
keywords: plugins, github
tags: [plugins, github]
sidebar: mydoc_sidebar
permalink: mydoc_github.html
---

The GitHub plugin provides stages for interaction with the [GitHubAPI](https://developer.github.com/v3/?). 
## Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-github" % "0.1-SNAPSHOT"
```

## Stages
Most stages in the GitHub plugin transform from one event type to another: EventToPushEvent, EventToIssuesEvent and EventToIssueCommentEvent.
They require no configuration. Additionally there is the GitHubEventsInput stage which streams events from the GitHub API, see [here](#githubeventsinput).

To see the datatypes used in this plugin for GitHub data see the [GitHubProtocol](https://github.com/joskuijpers/bep_codefeedr/blob/develop/codefeedr-plugins/codefeedr-github/src/main/scala/org/codefeedr/plugins/github/GitHubProtocol.scala). 

### GitHubEventsInput
Reads events from the '/event' endpoint of GitHub. Per poll often around +- 300 events are included, which means around 3 GitHub requests (100 per request). 

#### Configuration
```scala 
val numOfPolls = -1 //amount of polls before the stream stops. '-1' is default (unbounded)
val waitTime = 1000 //amount of milliseconds it should wait before doing a new poll. '1000' is default
val duplicateFilter = true //to enable a duplicate filter, which removes all duplicates from the stream. 'true' is default
val duplicateCheckSize = 1000000 //the amount of event id's to cache in case the duplicateFilter is enabled. 1000000' is default

val eventStage = new GitHubEventsInput(numOfPolls, waitTime, duplicateFilter, duplicateCheckSize)
```
**Note**: this stage makes use of the [KeyManager](../core/key-manager). Make sure to configure it properly with the keys you want to use for this stage.
The target of the key manager should be: `events_source`.


### Examples
In this section some sample pipelines will be discussed.
 
**Note**: These samples do not show any configuration like key management. See the sections above to see the configurable options.
#### Real-time PushEvent stream
```scala
new PipelineBuilder()
  .setBufferType(BufferType.Kafka)
  .append(new GitHubEventsInput())
  .append(new GitHubEventToPushEvent)
  .build()
  .startLocal()
```
This pipeline will create a real-time pushevents stream by reading from a the `/events` endpoint (GitHubEventsInput stage) and filter (& parse) the PushEvents (GitHubEventsToPushEvent stage).


## Notes
If the GitHub stages has interaction with the API, make sure to configure a KeyManager accordingly. More about GitHub ratelimits can be found [here](https://developer.github.com/v3/?#rate-limiting).

When using the `GitHubEventsInput` make sure choose a wait time based on the amount of keys/request you have available. E.g. if you have `5000` request p/h, you can make around `5000/3 = 1666` polls p/h, `3600/1666= 2.16` seconds between each poll.
