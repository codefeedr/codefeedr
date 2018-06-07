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
