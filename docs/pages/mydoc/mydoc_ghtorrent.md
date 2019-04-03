---
title: "GHTorrent"
keywords: plugins, ghtorrent
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_ghtorrent.html
---
{% include tip.html content="We offer access to GHTorrent data in CodeFeedr as a service. See this page for more information." %}

The GHTorrent plugin is mainly focused on mirroring the [original GHTorrent project](http://www.ghtorrent.org/) in CodeFeedr.
With the help of the GHTorrent plugin you can either hook into our CodeFeedr service or mirror (parts of) GHTorrent yourself.

## Installation
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12)
```scala
dependencies += "org.codefeedr" %% "codefeedr-ghtorrent" % "LATEST_VERSION"
``` 


## Stages
In general the GHTorrent plugin provides stages to mirror and parse (parts) of GHTorrent. In general, its important that you have access to the GHTorrent streaming service. This involves adding your public key to their repository. Please read [this page](http://ghtorrent.org/services.html) on details how to add your key.

After receiving access you need to create an SSH tunnel:
```bash
ssh -L 5672:streamer.ghtorrent.org:5672 ghtorrent@streamer.ghtorrent.org
```

After this point the GHTorrent plugin will take care of following the correct protocol defined by GHTorrent.

### GHTInputStage
This is the starting point for mirroring GHTorrent, without this stage you won't retrieve any data. This stage reads from the GHTorrent streaming service and parses it into a [Record](mydoc_ghtorrentspec.html#%0Aghtorrent-related%0A).
As described above, this stage won't work without having access to the GHTorrent streaming service and the SSH tunnel. 

#### Configuration
The GHTInputStage has to be configured with a set of routing keys. These keys determine which records you will receive from GHTorrent. Currently we support the following routing keys:

| Routing key | Data type |
|-------|--------|
| evt.commitcomment.insert | CommitCommentEvent |
| evt.create.insert | CreateEvent |
| evt.delete.insert | DeleteEvent  |
| evt.deployment.insert | DeploymentEvent |
| evt.deploymentstatus.insert | DeploymentStatusEvent |
| evt.fork.insert | ForkEvent |
| evt.gollum.insert | GollumEvent |
| evt.issuecomment.insert | IssueCommentEvent |
| evt.issues.insert | IssuesEvent |
| evt.member.insert | MemberEvent |
| evt.membership.insert | MemberShipEvent |
| evt.pagebuild.insert | PageBuildEvent |
| evt.public.insert | Public Event |
| evt.pullrequest.insert | PullRequestEvent |
| evt.pullrequestreviewcomment.insert | PullRequestReviewCommentEvent |
| evt.push.insert | PushEvent |
| evt.release.insert | ReleaseEvent |
| evt.repository.insert | RepositoryEvent |
| evt.status.insert | StatusEvent |
| evt.teamadd.insert | TeamAddEvent |
| evt.watch.insert | WatchEvent |
| ent.commits.insert | Commit |

Specification (in the form of Scala case classes) can be found on [this page](mydoc_ghtorrentspec.html).
  
```scala
val username = "wzorgdrager" //you need to specify a username to make the connection to GHTorrent streaming service unique. There is no default. 
val stageName = "ght_input" //the name of this stage (this will also be the name of topic in Kafka). Default is ght_input.
val hostname = "localhost" //the host-name to which you configured the SSH tunnel to GHTorrent. Default is localhost.
val port = 5672 //the port to which you configured the SSH tunnel to GHTorrent. Default is 5672.
val routingKeysFile = "routing_keys.txt" //the file in which you defined your routing keys (\n separated). Default is routing_keys.txt.

val inputStage = new GHTInputStage(username, stageName, hostname, port, routingKeysFile)
```

For instance in order to track all the commits and push events you define your `routing_file.txt` as:
```txt
ent.commits.insert
evt.push.insert
```

**Note:** Records are parsed based on the `routing_key` and its `content`, but the content itself is not parsed.
## Example use-case