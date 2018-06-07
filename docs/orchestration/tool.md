# Cluster Tool

This Flink cluster tool, named `cf-flink`, helps with starting and managing CodeFeedr pipelines. 

Pipeline programs for a Flink cluster are to be JARs without any external dependencies,
but without Scala and the core Flink dependencies. The codefeedr example project is correctly
configured to supply a JAR when running `sbt assembly`.

### Setting the Flink server

The tool accepts a `--host` parameter to set the Flink host. It is also possible to use the `FLINK_HOST` environment variable. This environment variable is overwritten by any supplied host. If no host configuration is found, `localhost:8081` is used.

### Commands

Below a list of commands. When an argument is given with `<>` it is mandatory.

#### Getting info of a pipeline

Get a bunch of info about a pipeline. This commands uploads the JAR to the Flink server
to analyze it. The output contains a list of stages in the pipeline. If any duplicate stage IDs
exist it will throw an error.

`cf-flink pipeline info <jarfile>`

##### Example output
```
Found 4 stages in JAR:
org.codefeedr.plugins.github.stages.GitHubEventsInput
org.codefeedr.plugins.mongodb.stages.MongoOutput
org.codefeedr.plugins.github.stages.GitHubEventToPushEvent
org.codefeedr.plugins.elasticsearch.stages.ElasticSearchOutput
```

#### Starting a pipeline
Start a whole pipeline from the given JAR file. The stages need to have unique IDs, otherwise an
error will be thrown. Only non-running stages will start.

`cf-flink pipeline start <jarfile>`

The JAR is uploaded to the Flink server, after which a list of stages is retrieved. 
Then each stage will be started __if and only if_ not a job with such stage name is already running.

#### Stopping a pipeline
Stops the whole pipeline from the given JAR file.

`cf-flink pipeline stop <jarfile>`

__Note:__ Any job with a stage ID corresponding to a stage inside the JAR will be cancelled, even
if it was started with another pipeline.

#### Starting a single stage
Start a single stage from the pipeline in the JAR. Useful when wanting to run only a part of the pipeline.

Will not start the stage if it already running.

`cf-flink stage start <jarfile> <stageId>`

#### Stopping a single stage
Stop any job with given stage ID attached. (See 'Stopping a pipeline')

`cf-flink stage stop <jarfile> <stageId>`

#### Getting a list of all jobs
Get a list of all jobs on the Flink server. By default shows only the jobs that are running.

`cf-flink jobs`

##### Arguments

To only print a list of JobIds (for automation), add `-q`.
To print also failed and completed jobs, add `-a`.

##### Example output

```
$ ./cf-flink.py jobs -a
JOBID					            STATUS		STAGE
35938dcf427054e6370fb4d0773e536b	cancelled	org.codefeedr.plugins.github.stages.GitHubEventsInput
a419ccd0c951b20cf68ce89a5497aa95	cancelled	org.codefeedr.plugins.github.stages.GitHubEventToPushEvent
945bb0eff271ec305a36f865bbe26d8a	cancelled	org.codefeedr.plugins.mongodb.stages.MongoOutput
d8f861ebf69e6b41afbbd8db5cfa19b4	running		org.codefeedr.plugin.twitter.stages.TwitterStatusInput
37f0b9fb2c5805c4be1e1c272f64eb58	running		org.codefeedr.plugins.elasticsearch.stages.ElasticSearchOutput
212fd6c7b01db8856fe8bf0be7dd8ae6	running		org.codefeedr.plugins.github.stages.GitHubEventsInput
2e8ad84d302c83a7579666e0fc58f3b9	running		org.codefeedr.plugins.github.stages.GitHubEventToPushEvent
5b53dadd9764de0db0bcbf2774144812	running		mongo_tweets
4adaf84ba632bfc5ab14565ed5fffdaf	running		elasticsearch_tweets
ce4c08231d4c99f60ecc8853c1a12922	running		org.codefeedr.plugins.mongodb.stages.MongoOutput
932adc63c03a871dfe4748c93537bb15	failed		org.codefeedr.plugins.elasticsearch.stages.ElasticSearchOutput
081239004221f4f18db23ac6eb42294f	failed		elasticsearch_tweets
```

#### Cancelling a job
Cancel a running job, without directly referencing a pipeline. Useful when the JAR file is not known / near.

`cf-flink cancel <jobId>`

To get the jobId, run the `jobs` command.

#### Listing all programs on the flink server
List all programs (JARs) currently on the Flink server.

`cf-flink program list`

##### Example output

```
PROGRAMID                                                                           FILENAME                                    TIMESTAMP
83d235a8-b158-4323-a1d2-63bcde4fd836_Flink Project-assembly-0.1-SNAPSHOT.jar		Flink Project-assembly-0.1-SNAPSHOT.jar	    1528371288000
4979efc9-9828-4e2b-93f4-2ab0f788b1cc_Flink Project-assembly-0.1-SNAPSHOT.jar		Flink Project-assembly-0.1-SNAPSHOT.jar	    1528368893000
b6dfa03e-2226-4c08-bca9-dbaa92974b9c_twitter.jar		                            twitter.jar	                                1528368882000
fa44221a-e04f-4d8d-8e7f-347737ae1793_twitter.jar		                            twitter.jar	                                1528368294000
```