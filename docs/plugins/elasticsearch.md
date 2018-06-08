# ElasticSearch

A plugin with an output stage for ElasticSearch, based on the sink from the Apache Flink project.

___NOTE___: The Apache Flink sink uses the ElasticSearch Java API which has been removed in version 5.2. To use this
output stage, you will need to run version 5.1.2 of ElasticSearch and open up port 9300. A new sink is in review on
the Flink repository.

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-elasticsearch" % "0.1-SNAPSHOT"
```

### Configuration

To use the ElasticSearch output stage, configure it with an index to write to, optionally a set of ElasticSearch
servers, and optionally extra configuration options. When no servers are given, it will try to connect to `localhost:9300`.

```scala
val es = new ElasticSearchOutput("my-index", Set("elasticsearch:9300"))
```

The event time is not sent to ElasticSearch. A Kibana index can be configured to take any date-line value as event time.

### Notes

In the current implementation, if the plugin loses connection with ElasticSearch, the whole Flink job fails and 
will need to be restarted.