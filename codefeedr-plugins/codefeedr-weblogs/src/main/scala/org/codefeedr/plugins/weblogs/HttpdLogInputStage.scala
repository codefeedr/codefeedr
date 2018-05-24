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
package org.codefeedr.plugins.weblogs

import java.time.{LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.codefeedr.stages.{InputStage, StageAttributes}

/**
  * Source that streams Apache log files
  *
  * @param absolutePath Absolute file path to the log
  */
class HttpdLogInputStage(absolutePath: String, stageAttributes: StageAttributes = StageAttributes()) extends InputStage[HttpdLogItem](stageAttributes) with Serializable {

  override def main(): DataStream[HttpdLogItem] = {
    pipeline.environment
      .readTextFile(absolutePath)
      .setParallelism(1)
      .flatMap(_.split('\n'))
      .flatMap(new LogMapper())
      .assignAscendingTimestamps(_.date.atZone(ZoneId.systemDefault()).toEpochSecond)
  }

}

private class LogMapper extends FlatMapFunction[String, HttpdLogItem] {

  lazy val dateFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z")
  lazy val pattern = """^(\S+) \S+ \S+ \[([\w:\/]+\s[+\-]\d{4})\] "(\S+)\s+(\S+)\s+(\S+)?\s*" (\d{3}) (\S+) ("[^"]*") ("[^"]*") ("[^"]*")""".r

  def flatMap(line: String, out: Collector[HttpdLogItem]): Unit = line match {
    case pattern(ipAddress, dateString, method, path, version, status, amountOfBytes, referer, userAgent, _*) => {
      val date = LocalDateTime.parse(dateString, dateFormatter)
      val amountOfBytesInt = if (amountOfBytes != "-") amountOfBytes.toInt else -1

      out.collect(HttpdLogItem(ipAddress, date, method, path, version, status.toInt, amountOfBytesInt, referer, userAgent))
    }
    case _ =>
  }

}