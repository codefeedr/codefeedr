package org.codefeedr.plugins.rss

import org.codefeedr.pipeline.{NoType, Pipeline, PipelineObject}

class RSSSource(url: String) extends PipelineObject[NoType, RSSItem] {
  override def main(pipeline: Pipeline): Unit = {

  }
}