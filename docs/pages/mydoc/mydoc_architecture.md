---
title: "Architecture"
keywords: codefeedr
tags: [architecture]
sidebar: mydoc_sidebar
permalink: mydoc_architecture.html
---
{% include important.html content="Work in progress!" %}
## Apache Flink
In order to understand the CodeFeedr architecture first a quick look
into Flink. A typical Flink job looks like this:
<p align="center"><img src="./images/flink_job.png"
style="width: 400px"></p>Data is read from an input source, a continuous
query is applied and finally the output of this query is streamed into
an output sink. Using the output of one job as input for another job is
not natively supported. Besides, creating complex streaming
architectures with many interconnected Flink jobs requires a lot of
boilerplate code. CodeFeedr tackles this.


## Pipeline
The main functionality of CodeFeedr is pipelining Flink jobs. A pipeline
is a directed acyclic graph. Data flow from one job to another is
coordinated by a buffer (default this buffer is [Apache
Kafka](https://kafka.apache.org)). Within a pipeline multiple _stages_
are connected, using a _buffer_. A pipeline is created using the
_PipelineBuilder_.  

### Stage
Since pipelines are directed acyclic graph in which we identify three
types of stages. A stage is nothing more than a _Flink job_.
- *InputStage*: the starting point of a pipeline, they are meant to
ingest data into the pipeline from an (external) source. An input stage
outputs to one or more different stages.
- *TransformStage*: intermediate stages of a pipeline, it transform data
from one stage to another (i.e. reads from one Flink job, transforms the
data and outputs to another Flink job). A transform stage can read from
multiple stages as well as outputting to multiple stages.  
- *OutputStage*: the end point of a pipeline, typically writing the data
to an (external) database. An output stage reads from one or more stages
and outputs to none.

### Buffer
Buffers are used to _flow_ data from one Flink job to another. We chose
to implement Kafka as default buffer since it is highly scalable,
fault-tolerant and extremely fast. However, you can
[implement](mydoc_buffer.html#write-your-own-buffer) and use your own
buffering system within a CodeFeedr pipeline.  

We use the [publish &
subscribe](https://kafka.apache.org/documentation/#producerapi)
functionality of Kafka for data flow. A stage outputs its data to a
Kafka topic whereas multiple stages can read from one or more topics.

In the image below you can see a CodeFeedr pipeline visualized.
**Note**: it shows Kafka as buffer in between stages whereas this can
be configured with your own buffer.

<p align="center"><img src="./images/codefeedr_pipeline.png"
style="width: 600px"></p>

### PipelineBuilder

## Plugins

## Deployment
In practical, CodeFeedr pipelines are nothing more than a set of Flink
jobs connected with (by default) Kafka. With the help of our
orchestration tools, you can both setup a Flink and Kafka cluster as
well as deploying and maintaining your pipeline on this cluster. More
specifically we provide:
  - A [Docker
configuration](https://github.com/codefeedr/codefeedr/tree/develop/tools/docker) file to setup both Kafka and Flink.
  - A [Python
script](https://github.com/codefeedr/codefeedr/blob/develop/tools/flink-cluster/cf-flink.py) to upload and manage your CodeFeedr pipeline on the Flink cluster.  


## Example use-case
Add example use-case here.


{% include links.html %}
