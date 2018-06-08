INTRO

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-twitter" % "0.1-SNAPSHOT"
```


### Configuration

```scala
val twitter = new TwitterStatusInput(...)
```

### Examples

The Twitter plugin also provides a stage to get the tweets regarding the current list of trending topics. See the
snipped below for its usage.

```scala
val twitterTrending = new TwitterTrendingStatusInput(...)
```

### Notes