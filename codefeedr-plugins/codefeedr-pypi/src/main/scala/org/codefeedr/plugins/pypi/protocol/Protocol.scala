package org.codefeedr.plugins.pypi.protocol

import java.util.Date

object Protocol {

  case class PypiRelease(title: String,
                         link: String,
                         description: String,
                         pubDate: Date)

}
