Plugin that provides input stages for web server logs.

Currently supports default Apache httpd access logs. NginX support is welcome through a pull request.

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-weblogs" % "0.1-SNAPSHOT"
```

### Configuration

The Apache Httpd log input creates a stream of `HttpdLogItem` events from a file. The `HttpdLogItem` case class
contains every part of a log line parsed into Scala format.

```scala
val log = HttpdLogInput("/var/httpd/access.log")
```

### Notes

This plugin currently does only read the file once and does not monitor for new changes. It is a simple proof of
concept of an input stage.
