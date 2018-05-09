package org.codefeedr.utilities

import scalaj.http.HttpResponse

class Http() extends Serializable {

  def getResponse(url: String): HttpResponse[String] = {
    scalaj.http.Http(url).asString
  }

}
