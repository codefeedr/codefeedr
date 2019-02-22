---
title: Plugins overview
tags: [plugins]
keywords: plugin, overview, plugins
permalink: mydoc_plugin_overview.html
sidebar: mydoc_sidebar
folder: mydoc
---
{% include important.html content="Work in progress!" %}
{% include warning.html content="Some plugin documentation pages are outdated!" %}
Plugins are sets of stages and other pieces of code that plug into the CodeFeedr system. The CodeFeedr 
repository contains a bunch of plugins already. All of them provide stages, and the MongoDB plugin 
also provides a key manager.

To use a plugin, simply add the project as a dependency in your SBT build file.

### What a plugin looks like

A plugin is a simple Scala project without a main class. It must depend on `codefeedr-core`. The CodeFeedr 
project has put its plugins in the `org.codefeedr.plugins` package, and their stages into a `stages` 
subpackage. This is not required.

### Creating a plugin
See [this](/mydoc_your_own_plugin.html) page.