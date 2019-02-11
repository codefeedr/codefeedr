/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codefeedr.stages.utilities

import org.apache.logging.log4j.scala.Logging
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
class HttpRequester(timeoutCap: Int = 32) extends Logging {

  @throws(classOf[Exception])
  def retrieveResponse(request: HttpRequest,
                       timeOut: Int = 1): HttpResponse[String] = {
    try {
      request.asString
    } catch {
      case x: Exception if x.getClass != classOf[RequestException] => {
        if (timeOut >= timeoutCap)
          throw RequestException(
            s"Requests timed out after $timeoutCap seconds.")

        logger.info(
          s"Failed doing a request retrying in ${timeOut * 2} in seconds.")
        Thread.sleep(timeOut * 2 * 1000)
        retrieveResponse(request, timeOut * 2)
      }
    }
  }

}
