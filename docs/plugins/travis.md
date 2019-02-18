The Travis plugin provides two transform stages to interact with the Travis API. It relies on the Github plugin because
because it uses the output of the Github push event stage (see [Github](github)).

### Installation

```scala
dependencies += "org.codefeedr" %% "codefeedr-travis" % "0.1-SNAPSHOT"
```

## Stages

The plugin contains two stages: 

- The `TravisFilterActiveReposTransformStage` takes a push-event stream from the GitHubEventToPushEvent Stage (see
[Github](github)) and filters it to only keep push events from repos that are active on Travis
 
- The `TravisPushEventBuildInfoTransformStage` takes a push-event from active Travis repositories stream and requests
the build information from Travis. The constructor only has one parameter:
  
    - `capacity`: to specify the amount of builds that are simultaneously requested per Flink thread (`100` is the 
    default).


### Configuration


**NOTE**: This plugin needs API access to TravisCI and requires keys. It will request keys from a key manager
with the target `travis`.

### Examples
```scala
new PipelineBuilder()
  .setBufferType(BufferType.Kafka)
  .append(new GitHubEventsInput())
  .append(new GitHubEventToPushEvent)
  .append(new TravisFilterActiveReposTransformStage())
  .append(new TravisPushEventBuildInfoTransformStage(capacity = 10))
  .build()
  .startLocal()
```
This pipeline will create a real-time Travis build stream by:

- Reading from a the `/events` endpoint (GitHubEventsInput stage)
- Filter (& parse) the PushEvents (GitHubEventsToPushEvent stage)
- Filter the push events from repositories that are active on Travis
- Request build information of those push events

**Note**: These samples do not show configuration of for instance key management. See the sections above to show the
configurable options.

