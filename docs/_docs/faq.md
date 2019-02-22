---
title: Frequently Asked Questions
permalink: /docs/faq/
---

**1. I'm getting the following error** `could not find implicit value for evidence parameter of type org.apache.flink.api.common.typeinfo.TypeInformation[SomeType]`.

Make sure to import the TypeInformation implicits from Flink: `import org.apache.flink.streaming.api.scala._`. Also see [this](https://flink.apache.org/gettinghelp.html#got-an-error-message).
