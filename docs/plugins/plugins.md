Plugins are sets of stages and other pieces of code that plug into the CodeFeedr system. The CodeFeedr 
repository contains a bunch of plugins already. All of them provide stages, and the MongoDB plugin 
also provides a key manager.

To use a plugin, simply add the project as a dependency in your SBT build file.

### What a plugin looks like

A plugin is a simple Scala project without a main class. It must depend on `codefeedr-core`. The CodeFeedr 
project has put its plugins in the `org.codefeedr.plugins` package, and their stages into a `stages` 
subpackage. This is not required.


### Creating a plugin

It is naturally possible to create a new plugin.

Most of the time a plugin would have stages. For each input and output stage you likely need a Flink Source and
Flink SinkFunction. These are normal implementations as per the Flink infrastructure. Wrapping them in stages is then very easy.

Adding transform stages is like writing any other stage, with inputs and an output with Flink processing in between.

Look into the existing plugins for examples.