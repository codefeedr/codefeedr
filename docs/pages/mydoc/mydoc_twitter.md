---
title: "Twitter"
keywords: plugins, twitter
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_twitter.html
---

This plugin provides stages related to the Twitter API. 
It makes uses of the [twitter4s](https://github.com/DanielaSfregola/twitter4s) library, for detailed documentation see their GitHub page.

## Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-twitter" % "0.1-SNAPSHOT"
```


## Stages
Currently two stages are provided related to Twitter statuses. The `TwitterStatusInput` is an InputStage which
streams Twitter statuses based on some filters.  The Twitter plugin also provides the `TwitterTrendingStatusInput` to get the tweets regarding the current list of trending topics.

### Configuration
For all the Twitter related stages you must provide the following case classes:

```scala 
case class ConsumerToken(key: String, secret: String)
case class AccessToken(key: String, secret: String)
```

Those tokens can be generated using a [Twitter app](https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html).

#### TwitterStatusInput
The filters of this stage are explained [here](https://github.com/joskuijpers/bep_codefeedr/blob/fd14096544fe5a2390a356bd5cb8781a52e28db8/codefeedr-plugins/codefeedr-twitter/src/main/scala/org/codefeedr/plugin/twitter/stages/TwitterStatusInput.scala#L37) and should be passed through the constructor.

**Note**: At least 1 filter should be given, see [here](https://developer.twitter.com/en/docs/tweets/filter-realtime/api-reference/post-statuses-filter.html).

#### TwitterTrendingStatusInput
For the TwitterTrendingStatusInput a `sleepTime` should be given, this is the amount of minutes to wait before dynamically refreshing the trending topics
and restarting the stream. By default this is 15 minutes. 

**Note:** Retrieving the trending topics is a request that is cached for 5 minutes at the Twitter API. Therefore, setting the `sleepTime` lower than 5 minutes doesn't make any sense. 

## Notes
Twitter also ratelimits its streams, [twitter4s](https://github.com/DanielaSfregola/twitter4s) takes care of that. To get more information
on this ratelimit see [here](https://developer.twitter.com/en/docs/basics/rate-limiting.html).
