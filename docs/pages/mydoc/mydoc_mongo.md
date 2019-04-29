---
title: "MongoDB"
keywords: plugins, mongo
tags: [plugins]
sidebar: mydoc_sidebar
permalink: mydoc_mongo.html
---

The MongoDB plugin provides both an input and output stage for MongoDB. It allows for reading historic
data from a Mongo collection, or creating such data by streaming into it. It also adds support for using MongoDB as a
key manager

## Installation
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codefeedr/codefeedr-core_2.12)
```scala
dependencies += "org.codefeedr" %% "codefeedr-plugin-mongodb" % "LATEST_VERSION"
```

## Stages

Any events sent to the mongo output stage are automatically parsed into JSON and converted to BSON. If you want to save
only a certain set of values, map them to a smaller case class first. It is not possible to use transient properties
as they do not necessarily survive the pipeline buffer.

If an event time is available to the output stage, it is automatically added to the document with the `_eventTime` key.
This value is in turn used to re-create the event time for the stream when reading with the input stage. The
store value is in milliseconds since epoch.

### Configuration

Configuring a mongo stage only requires the database and collection names to read from / write into.
Optionally a server address can be supplied. It defaults to localhost on the default mongo port.

```scala
val mongoIn = new MongoInput[MyEvent]("db", "myCollection", "mongodb://mongo:1234")
val mongoOut = new MongoOutput[MyEvent]("db", "myCollection", "mongodb://mongo:1234")
```

If the server requires authentication, use the URI format: `mongodb://username:password@mongo:1234`.

#### Filtering input

The mongo input stage accepts a BSON query to allow for selective data retrieval. A query is a `MongoQuery` object
that is a utility wrapper of the Mongo Scala Driver queries. See the snipped below for the options.

```scala

// Query reading only data since given event time
val query = MongoQuery.from(new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime)

// Custom query
val query = new MongoQuery(eq("myKey", "myVal"))

val mongoIn = new MongoInput[MyEvent]("db", "myCollection", query = query)
```

### Notes

Data is read in the order it is in the collection. Elements are not necesserily written in order to the collection,
however, because of parallelism of the sink. Do not assume event time order in the stream from an input stage.

## Key Manager

The mongoDB key manager implements the full key manager for mongoDB. It supports keeping track of the number of calls a key has left and automatic refreshing of that number over an interval.

### Configuration

Configuring the pipeline to use mongoDB is easy. All configuration is optional: by default, the manager looks for a local instance of mongoDB and uses the `codefeedrKeyManager` collection in the database `db`.

```scala
val km = new MongoKeyManager()
```

To use your own values:

```scala
val km = new MongoKeyManager("db", "myCollection", "mongodb://server:12345")
```

#### Adding keys

Every document in the collection represents a single key. A document has the following format (with example values):

```json
{
   "target": "github",
   "key": "AJKGFUG3986GHKH",
   "numCallsLeft": 9,
   "limit": 10,
   "interval": 60000,
   "refreshTime": "2018-04-23T18:25:43.511Z"
}
```

This creates a key for the github target, with a limit of 10 calls per 60 seconds (60000ms). It can be refreshed starting at 23rd of april 2018. (So it will be refreshed the moment a key is requested). In the current interval it still has 9 calls left.

#### Removing keys

To remove a key, simply delete the document from the collection.


## Notes
If the GitHub stages has interaction with the API, make sure to configure a KeyManager accordingly. More about GitHub ratelimits can be found [here](https://developer.github.com/v3/?#rate-limiting).

When using the `GitHubEventsInput` make sure choose a wait time based on the amount of keys/request you have available. E.g. if you have `5000` request p/h, you can make around `5000/3 = 1666` polls p/h, `3600/1666= 2.16` seconds between each poll.
