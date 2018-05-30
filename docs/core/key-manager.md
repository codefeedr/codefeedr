Codefeedr provides a key manager for managing expiring access keys, such as REST API authorization keys.

API keys are often rate limited on a time interval. For example, a key for GitHub could allow for 1000 calls in a single day. To do a proper streaming job, no large moments of time should be without a valid key, and thus you want to supply multiple keys. With the key manager the keys can be managed together with how many of these calls can still be made per key. It optimizes the key selection to increase chances a valid key stays available.

### Current implementations
Currently Codefeedr contains two implementations of the key manager: static and redis.

The static key manager is a simple map of target+key. The key from this map is used. No call counters are kept (the keys are 'unmanaged') and you can't use multiple keys per target. This is ideal for a very quick setup and test when all you need is to supply a single REST API key.

The redis key manager is a fully compliant implementation of the key manager. Redis contains a list of keys per target and keeps their validity: how many calls can still be done with the key. When requesting a key, the number of calls it will be used for is noted. It also automatically refreshes those counters. See more on the [redis page](redis-key-manager).

### Configuring
To use a key manager in any pipeline object, such key manager first needs to be configured. This is done when building the pipeline. The simples use case is to supply a single unmanaged key to a `StaticKeyManager`:

```scala
val builder = new PipelineBuilder()
  .setKeyManager(new StaticKeyManager("github" -> "kjfh983wfbw8bv80nw24bv"))
  // other building  

val pipeline = builder.build()
```

### Using in a pipeline object

A pipeline object can ask the key manager from the pipeline. Then it can ask for a key for the target.

```scala
def transform(source: DataStream[MyObject]): DataStream[MyOtherObject] = {
  val key = pipeline.keyManager.request("github")
}
```

The key is a `Option[ManagedKey]`. The managed key contains both the value and the number of calls remaining after using the one requested.

#### Using the key multiple times

When the object needs to do multiple requests it can tell so to the key manager. A kill will be found that has at least that many calls left.

```scala
pipeline.keyManager.request("github", 3)
```

The key manager assumes the key is never used more often than requested. If a key is used less often than requested, that count is lost until the next refresh of the keys.

## mongoDB
The mongoDB key manager implements the full key manager for mongoDB. It supports keeping track of the number of calls a key has left and automatic refreshing of that number over an interval.

### Configuring

Configuring the pipeline to use mongoDB is easy. All configuration is optional: by default, the manager looks for a local instance of mongoDB and uses the `codefeedrKeyManager` collection in the database `db`. 

```scala
val km = new MongoKeyManager()
```

To use your own values:

```scala
val km = new MongoKeyManager("db", "myCollection", "mongodb://server:12345)
```

### Adding keys

Every document in the collection represents a key. A document has the following format (with example values):

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

### Removing keys

To remove a key, simply delete the document from the collection.
