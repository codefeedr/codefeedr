<p align="center"><img src="logo.png" width="300px"/></p>

- - - -

CodeFeedr
--------------------------

[![Build
Status](https://travis-ci.org/codefeedr/codefeedr.svg?branch=develop)](https://travis-ci.org/codefeedr/codefeedr) [![BCH compliance](https://bettercodehub.com/edge/badge/codefeedr/codefeedr?branch=develop)](https://bettercodehub.com/)
[![Coverage
Status](https://coveralls.io/repos/github/codefeedr/codefeedr/badge.svg?branch=develop)](https://coveralls.io/github/codefeedr/codefeedr?branch=develop) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![University](https://img.shields.io/badge/university-tudelft-blue.svg)](http://swerl.tudelft.nl/bin/view/Main/WebHome)

A framework for easily building Flink streaming programs. Documentation
can be found [here](http://codefeedr.org/codefeedr).
## Contributors
Main contributors:
- [Wouter
Zorgdrager](https://www.linkedin.com/in/wouter-zorgdrager-a4746512a/)

Former contributors:
- [Jos Kuijpers](https://nl.linkedin.com/in/jos-kuijpers-4b714032)
- [Joris Quist](https://www.linkedin.com/in/joris-quist-a44245170)


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

### From IntelliJ

Install the latest IntelliJ. Go to the IntelliJ preferences and install
the Scala plugin. Then

1. File -> New -> Project with existing sources from within IntelliJ or
"Import project" from the IntelliJ splash screen
2. Select the top level checkout directory for CodeFeedr.
3. On the dialog that appears, select "SBT"
4. Click Next and then Finish
5. From the "SBT Project Data To Import", select all modules

In order to run your application from within IntelliJ, you have to
select the classpath of the 'mainRunner' module in  the run/debug
configurations. Simply open 'Run -> Edit configurations...' and then
select 'mainRunner' from the "Use  classpath of module" dropbox.

## Run documentation locally
In order to run the documentation locally, make sure to have [Ruby and Rubygem](https://guides.rubygems.org/rubygems-basics/) installed.

1. Install [Jekyll](https://jekyllrb.com/): `gem install jekyll`
2. Install dependencies: `gem install bundler`
3. Move to the docs directory: `cd docs`
4. Serve the website: `jekyll serve`
By default the documentation will be served at [http://127.0.0.1:4000](http://127.0.0.1:4000)

## Use orchestration tools
The orchestration tools are located under [`/tools`](/tools). They depends on:
- Python
- Docker
- Redis