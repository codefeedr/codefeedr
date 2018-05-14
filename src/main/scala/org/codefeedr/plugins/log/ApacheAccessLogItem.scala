package org.codefeedr.plugins.log

import java.time.LocalDateTime

import org.codefeedr.pipeline.PipelineItem

case class ApacheAccessLogItem(ipAdress: String,
                               date: LocalDateTime,
                               request: String,
                               status: Int,
                               amountOfBytes: Int,
                               referer: String,
                               userAgent: String) extends PipelineItem