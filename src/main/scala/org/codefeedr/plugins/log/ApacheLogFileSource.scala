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
 *
 */
package org.codefeedr.plugins.log

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.Source

/**
  * Source that streams Apache log files
  * @param absolutePath Absolute file path to the log
  */
class ApacheLogFileSource(absolutePath: String) extends Source[ApacheAccessLogItem] with Serializable {

  override def main(): DataStream[ApacheAccessLogItem] = {
    val lineToLogItem = lineToLogItemFunction()
    pipeline.environment.readTextFile(absolutePath).flatMap(_.split('\n')).map(v => lineToLogItem(v)) // lineToLogItem(v))
  }

  def lineToLogItemFunction(): String => ApacheAccessLogItem = {
    line: String => {
      val pattern = "^(\\S+) \\S+ \\S+ \\[([\\w:\\/]+\\s[+\\-]\\d{4})\\] \"(\\S+\\s*\\S+\\s*\\S+)?\\s*\" (\\d{3}) (\\S+) (\"[^\"]*\") (\"[^\"]*\") (\"[^\"]*\")".r

      val groups =  pattern.findFirstMatchIn(line).orNull

      var toReturn: ApacheAccessLogItem = ApacheAccessLogItem("", null, "", 0, 0, "", "")

      try {
        val ipAddress = groups.group(1)
        val dateString = groups.group(2)
        val request = groups.group(3)
        val status = if (groups.group(4).matches("^[0-9]+$")) groups.group(4).toInt else -1
        val amountOfBytes =  if (groups.group(5).matches("^[0-9]+$")) groups.group(5).toInt else -1
        val referer = groups.group(6)
        val userAgent = groups.group(7)

        // 06/Jan/2017:04:20:17 +0100
        val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
        val date = LocalDateTime.parse(dateString, formatter)

        toReturn = ApacheAccessLogItem(ipAddress, date, request, status, amountOfBytes, referer, userAgent)

      } catch {
        // This means that a line couldn't be parsed with the regex, this means that an empty item will be returned
        case  _: Throwable =>
      }

      toReturn
    }
  }

}
