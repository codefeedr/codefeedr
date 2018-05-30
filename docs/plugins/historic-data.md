Historic Data is a feature for using a stored set of data as stream input. It is useful for testing streaming algorithms.

It is also possible to build such database by streaming into it.

## mongoDB

The mongoDB plugin has both an input stage and an output stage. The input stage is used to get data from a mongoDB collection. It maps the mongo BSON document to a case class type. If the document contains an `_eventType` number field, it is used as the event time of the element.

Writing to mongo can be done using the output stage. If the element has an event time, it is added to the document automatically. The input object is written as BSON directly.

### Example

The mongoDB instance contains a collection of events, in the format `{name:<string>,eventType:<string>,_eventTime:<number>}`. The data is read and printed as JSON. Each Event would have its event time set.

```scala
case class Event(name: String, eventType: String)

val pipeline = new PipelineBuilder()
  .append(new MongoInputStage[Event]("myDb", "events"))
  .append(new JsonPrinter[Event]())
  .build()

pipeline.startLocal()
```

Writing to mongoDB is just as easy. Lets write to an external server:

```scala
case class Event(name: String, eventType: String)

val pipeline = new PipelineBuilder()
  .append(new InputStream())
  .append(new MongoOutputStage[Event]("bigDb", "events", "mongodb://myserver.org:5000))
  .build()

pipeline.startLocal()
```