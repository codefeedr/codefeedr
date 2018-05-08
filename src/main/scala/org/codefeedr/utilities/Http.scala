package org.codefeedr.utilities

import scalaj.http.HttpResponse

class Http() {

  def getResponse(url: String): HttpResponse[String] = {
    scalaj.http.Http(url).asString
  }

}
