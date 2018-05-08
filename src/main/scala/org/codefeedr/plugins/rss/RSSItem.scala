package org.codefeedr.plugins.rss

import java.time.LocalDateTime

import org.codefeedr.pipeline.PipelinedItem

case class RSSItem( title: String,
                    category: String,
                    link: String, // URL,
                    pubDate: LocalDateTime,
                    guid: String
                  ) extends PipelinedItem
