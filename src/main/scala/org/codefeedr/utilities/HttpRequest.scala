package org.codefeedr.utilities

import scalaj.http.{Http, HttpResponse}

class Http() {

  def getResponse(url: String): HttpResponse[String] = {
    Http(url).asString
  }

}
