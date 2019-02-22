---
title: "RSS"
keywords: plugins, rss
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_rss.html
---

A plugin with a stage for reading from a basic RSS feed using polling.

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-rss" % "0.1-SNAPSHOT"
```

### Configuration

An RSS input stage requires the address of the feed, the date format used in the `pubDate`, and the number of
milliseconds between each poll.

```scala
val dateFormat = "EEE, dd MMMM yyyy HH:mm:ss z"
val pollingInterval = 1000 // once per second
val rss = new RSSInput("http://example.com/feed.xml", dateFormat, pollingInterval)
```

The result is a stream of `RSSItem` objects with title, date, link, category and guid.
