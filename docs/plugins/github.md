## GitHub
The GitHub plugin has several stages. In the section below each stage will be discussed and lastly some sample pipelines will be discussed. See [GitHubProtocol](https://github.com/joskuijpers/bep_codefeedr/blob/develop/src/main/scala/org/codefeedr/plugins/github/GitHubProtocol.scala) for all the case classes used.

### Stages
#### GitHubEventsInput
Reads events from the '/event' endpoint of GitHub. Per poll often around +- 300 events are included, which means around 3 GitHub requests (100 per request). In the list below the constructor parameters are explained:

- `numOfPolls`: to specify the amount of polls before it stops, `-1` for unbounded (`-1` is default).
- `waitTime`: to specify the amount of milliseconds it should wait before doing a new poll. Make sure choose a wait time based on the amount of keys/request you have available. E.g. if you have `5000` request p/h, you can make around `5000/3 = 1666` polls p/h, `3600/1666= 2.16` seconds between each poll. (`1000` is default).
-  `duplicateFilter`: to enable a duplicate filter. This source runs single-threaded, so the event id's can be cached to check for duplicates. (`false` is default).
- `duplicateCheckSize`: the amount of event id's to cache in case the duplicateFilter is enabled. (`1000000` is default)

**Note**: this stage makes use of the [KeyManager](#key-manager). Make sure to configure it properly with the keys you want to use for this stage. 

#### GitHubEventToPushEvent
This stage reads from the event timeline and filters it into a push-event stream. 

### Sample pipelines
In this section some sample pipelines will be discussed.
 
**Note**: These samples do not show configuration of for instance key management. See the sections above to show the configurable options.
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