package org.codefeedr.plugins.rss

import java.util.Date

import org.codefeedr.pipeline.PipelinedItem

case class RSSItem(
                    title: String,
                    category: String,
                    link: String, // URL,
                    pubDate: Date,
                    guid: String
                  ) extends PipelinedItem
