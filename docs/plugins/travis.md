## Travis
The Travis plugin has several stages. In the section below each stage will be discussed and lastly some sample pipelines
will be discussed. See [TravisProtocol](https://github.com/joskuijpers/bep_codefeedr/blob/develop/codefeedr-plugins/codefeedr-travis/src/main/scala/org/codefeedr/plugins/travis/TravisProtocol.scala) for all the case classes used.

### Stages
#### TravisFilterActiveReposTransformStage
Takes a push-event stream from the GitHubEventToPushEvent Stage (see [Github](github)) and filters it to only keep push 
events from repos that are active on Travis


#### TravisPushEventBuildInfoTransformStage
Takes a push-event from active Travis repositories stream and requests the build information from Travis

- `capacity`: to specify the amount of builds that are simultaneously requested per Flink thread (`100` is default).

**Note**: this stage makes use of the [KeyManager](../core/key-manager). Make sure to configure it properly with the
keys you want to use for this stage. It looks for the keys that are under the `travis` key.

### Sample pipelines
In this section some sample pipelines will be discussed.
 
**Note**: These samples do not show configuration of for instance key management. See the sections above to show the
configurable options.

#### Real-time Travis builds stream
```scala
new PipelineBuilder()
  .setBufferType(BufferType.Kafka)
  .append(new GitHubEventsInput())
  .append(new GitHubEventToPushEvent)
  .append(new TravisFilterActiveReposTransformStage())
  .append(new TravisPushEventBuildInfoTransformStage())
  .build()
  .startLocal()
```
This pipeline will create a real-time build stream by:
- Reading from a the `/events` endpoint (GitHubEventsInput stage)
- Filter (& parse) the PushEvents (GitHubEventsToPushEvent stage)
- Filter the push events from repositories that are active on Travis
- Request build information of those push events