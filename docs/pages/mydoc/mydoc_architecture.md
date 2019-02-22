---
title: "Architecture"
keywords: codefeedr
tags: [architecture]
sidebar: mydoc_sidebar
permalink: mydoc_architecture.html
---
{% include important.html content="Work in progress!" %}
## Apache Flink
In order to understand the CodeFeedr architecture first a quick look into Flink.
A typical Flink job looks like this:
{% include image.html file="flink_job.png" alt="Flink job" max-width="500" %}
Data is read from an input source, a continuous query is applied and finally the output of this query is streamed into an output sink.
Using the output of one job as input for another job is not natively supported. Besides, creating complex streaming architectures with many interconnected Flink jobs requires a lot of boilerplate code. This is solved by CodeFeedr.


## Pipeline
The main functionality of CodeFeedr is pipelining Flink jobs. Data flow from one job to another is coordinated by a buffer (default this buffer is [Apache Kafka](https://kafka.apache.org)).

### Stage

### Buffer

### Pipeline

## Example use-case
Add example use-case here.


{% include links.html %}
