Bachelor Project Codefeedr
--------------------------

A framework for easily building Flink streaming programs

## Configuring the build environment

### Command line

The project is build with SBT. To install SBT, do:

* Mac: `brew install sbt`
* Debian/Ubuntu: `apt-get install sbt`

Run `sbt`. Then at the SBT console:

- `compile` to compile
- `run` to run the default class
- `test` to run the tests
- `clean` to clean
- `scalafmt` to format according to project guidelines
- `scalafmt::test` to check if format according to project guidelines

### From IntelliJ

Install the latest IntelliJ. Go to the IntelliJ preferences and install the
Scala plugin. Then

1. File -> New -> Project with existing sources from within IntelliJ or "Import project" from the
IntelliJ splash screen
2. Select the top level checkout directory for CodeFeedr.
3. On the dialog that appears, select "SBT"
4. Click Next and then Finish
5. From the "SBT Projet Data To Import", select all modules

In order to run your application from within IntelliJ, you have to select the classpath of the
'mainRunner' module in  the run/debug configurations. Simply open 'Run -> Edit configurations...'
and then select 'mainRunner' from the "Use  classpath of module" dropbox.

It is recommended to install the `scalafmt` plugin and turn on the 'Format on file save option' in the
IntelliJ preferences panel.
