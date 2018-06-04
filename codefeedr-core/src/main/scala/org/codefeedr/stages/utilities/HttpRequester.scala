package org.codefeedr.stages.utilities

import scalaj.http.{HttpRequest, HttpResponse}

/**
  * Exception thrown when something goes wrong during a request to an API.
  *
  * @param message the exception message.
  * @param cause   the exception cause.
  */
final case class RequestException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

/**
  * Requests http requests multiple times until it succeeds or the timeoutCap is exceeded.
  * @param timeoutCap Cap at which the timeout stops growing and stops trying instead.
  */
class HttpRequester(timeoutCap: Int = 32) {

  @throws(classOf[Exception])
  def retrieveResponse(request: HttpRequest, timeOut: Int = 1): HttpResponse[String] = {
    try {
      request.asString
    } catch {
      case x: Exception if  x.getClass != classOf[RequestException] =>
      {
        if (timeOut >= timeoutCap) throw RequestException(s"Requests timed out after $timeoutCap seconds.")

        println(s"Failed doing a request retrying in ${timeOut * 2} in seconds.")
        Thread.sleep(timeOut * 2 * 1000)
        retrieveResponse(request, timeOut * 2)
      }
    }
  }

}
