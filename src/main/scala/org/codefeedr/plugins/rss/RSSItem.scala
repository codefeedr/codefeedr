package org.codefeedr.plugins.rss

import java.time.LocalDateTime
import java.util.Date

import org.codefeedr.pipeline.PipelineItem

case class RSSItem(
                    title: String,
                    category: String,
                    link: String, // URL,
                    pubDate: LocalDateTime,
                    guid: String
                  ) extends PipelineItem
