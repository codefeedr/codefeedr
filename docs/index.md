<p align="center"><img src="logo.png" width="300px"/></p>

- - - -
This wiki provides documentation on the plugin infrastructure of the [CodeFeedr project](https://codefeedr.github.io/). Next to that, we provide information to create plugins yourself. 

## What is CodeFeedr
CodeFeedr is a infrastructure built on top of [Flink](https://flink.apache.org/) enabling to pipeline multiple Flink jobs using a message broker like [Kafka](https://kafka.apache.org/).
This allows you to easily setup a [DAG](https://en.wikipedia.org/wiki/Directed_acyclic_graph) of interconnected Flink jobs.
