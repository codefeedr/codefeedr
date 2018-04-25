
protocol Buffer[T] {
    func getSource() throws -> DataStreamSource[T]
    func getSink() throws -> DataStreamSink[T]
}

// Run environment, from ENVIRONMENT variables
class Environment {
    //    get/set

    let streamEnvironment: StreamExecutionEnvironment

    init() {
        streamEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    }
}

class SocketBuffer[T]: Buffer[T] {
    init() {

    }

    func getSource() throws -> FlinkSource[T] {
        return FlinkTextSocket
    }
}

class KafkaBuffer[T]: Buffer[T] {}

// Created by an app, given to POs and Buffers. Has settings
class Context {
    var pipeline: Pipeline?
    var keyManager: KeyManager?

    var bufferType: Class = SocketBuffer.self

    // Get pipeline?
    func createPipeline[Out](object: FirstPipelineObject[Out]) -> Pipeline[None,Out] {
        return pipeline
    }

    func start() {

    }
}

class PipeLineObjectAttributes {

}

// Object in the pipeline. Implemented by plugin
class PipelineObject[In,Out] {
    let context: Context
    var sourceBuffer: AbstractBuffer?
    var sinkBuffer: AbstractBuffer?

    init(context: Context) {
        self.context = context
    }

    func createSource() -> DataSource[In] {
        // if NOT In == None

        // Source created using Buffer type (socket or Kafka)
        // configured using context info

        if sourceBuffer == nil {
            sourceBuffer = BufferType(self)
        }

        return sourceBuffer!.createSource()
    }

    // History
    func createHistorySource(type????) -> DataSource[Int] {

    }

    func createSink() -> DataSink[Out] {
        // if NOT Out == None

        // Sink created using Buffer type (socket or Kafka)
        // configured using context info

        if sinkBuffer == nil {
            sinkBuffer = BufferType(self)
        }

        return sinkBuffer!.createSink()
    }

    func getStorageSource[T](key: String, collection: String) -> DataSource[T] {
        // From context, get info on what the storage is
        // Provide proper source
    }

    func getStorageSink[T](key: String, collection: String) -> DataSink[T] {
        // From context, get info on what the storage is
        // Provide proper sink
    }

    // some way to get name of kafka topic from parent
    class func getKafkaTopic() -> String {
        return "GitHub"
    }

    func main(environment: Environment) {
    }
}

class FirstPipelineObject[Out]: PipelineObject[None,Out] {

}

protocol Job[In]: PipelineObject[In,None] {}

//class Pipeline[In,Out] {
//    private let object = [PipelineObject[U,V]]
//
//    func append[T](object: PipelineObject[Out,T]) -> Pipeline[In,T]
//}

////////////////////////////////////////////////////
/// Storage abstraction
////////////////////////////////////////////////////

protocol Storage {
    // Called by context, when needed
    init(attributes) {
    }

    func createSource[T](collection: String) -> DataSource[T]
    func createSink[T](collection: String) -> DataSink[T]
}

class MongoStorage {
    init(attributes) {

    }

    func createSource[T]() {

    }

    class FlinkMongoSource
    class FlinkMongoSink
}

////////////////////////////////////////////////////
/// KV Storage, key manager
////////////////////////////////////////////////////

// Abstract
// Generic key value store.
protocol KeyValueStore {
    func get[T]() -> T?
    func set[T](path: String, val: T)
    func has(path: String) -> Bool
    // watch?
}

class ZookeeperKVStore: KeyValueStore {

}

protocol KeyManager {
    func request(type: String, numCalls: Int) -> String?
}

// Stores single key, always uses that key
class StaticKeyManager: KeyManager {
    let keys: [String: String]

    init(keys: [String: String]) {
        self.keys = keys
    }

    func request(type: String, numCalls: Int) -> String? {
        return keys[type]
    }
}

class ZookeeperKeyManager: ZookeeperKVStore, KeyManager {
    init(host: String, port: Int, root: String) {
        super.init(host, port, root)
    }

    func addKey(key: String, numberOfUses: Int, resetPolicy: ResetPolicy) {

    }

    func request(type: String, numCalls: Int) -> String? {
        // use .get and .set
    }
}

////////////////////////////////////////////////////
/// Example program
////////////////////////////////////////////////////


class Main {
    func main(args: [String], env: [String: String]) {
        let JobRunner = JobRunner(env) // ? For environment setup stuff?

        let job = new MyJob()

        jobRunner.startJob(job)
    }
}

class Tweet {
    let id: Int
    let text: String
    let retweeted: Bool
    let createdAt: Date
    let retweetCount: Int

    func map(map: Map) {
        id -> map["id"]
        text -> map["text"]
        retweeted -> map["retweeted"]
        retweetCount -> map["retweet_count"]
        createdAt -> (map["created_at"], DateParser())
    }
}

class TweetSource[None, Tweet] {
    func main(environment: Environment) {
        let env = environment.streamEnvironment

        env
            .readTextFile("../tweets.gz")
            .flatMap(JsonMapper[Tweet]())
            .assignAscendingTimestamps(_.createdAt)
            .sink(getSink()) // TODO: is this typed??
    }
}

class MyJob: Job[Tweet] {
    func setup(environment: Environment) {

        //// Build the pipeline

        let pipeline = context
                    .startWith(Twitter.self) // -> PipelineBuilder
                    .thenRun(LogObject.self) // -> PipelineBuilder
                    .thenRunJob()            // -> PipelineBuilder
                    .build()                 // -> Pipeline
        // x.alsoRun(Second.self)

        context.pipeline = pipeline


        //// Configure services

        // Add new buffer
        context.registerBufferService(type: "rabbitmq", class RabbitMQBuffer.self)

        // Create Kafka attributes (host, port)
        let kafkaAttrs = ["hostname": "localhost", "port": 7436]
        // Set buffer type to Kafka (attributes)
        context.setBuffer("kafka", kafkaAttrs)

        // Register new storage service
        context.registerStorageService(type: "mongo", class: MongoStorage.self)

        // Add storage models: (key, type, storageAttributes)
        let mongoAttrs = ["hostname": "localhost", "port": 1234]
        context.addStorage("history", "mongo", mongoAttrs)

    }

    func main(environment: Environment) {
        getSource()
            .map { tweet => (tweet.id, tweet.retweetCount) }
            .keyBy(0)
            .window(SlidingProcessingTimeWindows.of(Time.days(2), Time.hours(1))) // sliding window of 2 days, report every 1 hour
            .sum(1)
            .print()
    }

    func main2(environment: Environment) {
        let sourceAttributes = KafkaSourceAttributes(topic: "MyTopic") // subclasses SourceAttributes. Also used to define what the actual source is

        getSource()
            .map { tweet => (tweet.id, tweet.retweetCount) }
            .keyBy(0)
            .join(getExtraSource(sourceAttributes))
            .window(SlidingProcessingTimeWindows.of(Time.days(2), Time.hours(1))) // sliding window of 2 days, report every 1 hour
            .sum(1)
            .print()
    }
}


class LogObject[T]: PipelineObject[T,T] {

    func setup() {

    }

    func main() {
        getSource()
            .print()
    }

    class func getKafkaTopic() {
        return parent.getKafkaTopic()
    }
}

LogObject.getKafkaTopic()

getSource[T](LogObject.self) -> DataStream[T]


// TODO: system to give attributes to POs. For example path of historic data
