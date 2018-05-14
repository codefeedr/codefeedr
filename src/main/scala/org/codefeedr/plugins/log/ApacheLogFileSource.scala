package org.codefeedr.plugins.log

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline.Source

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
        case  _=>
      }

      toReturn
    }
  }

}
