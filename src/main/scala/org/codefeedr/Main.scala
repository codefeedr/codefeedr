package org.codefeedr


import java.text.SimpleDateFormat
import java.util.Date

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time

case class LogLine(ip: String, date: Date, target: String, responseCode: Int, contentSize: Int, referrer: String, UserAgent: String)

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val lineRegex = """(\S+) \S+ \S+ \[(.+)\] "([^"]+)" (\d+) (\d+) "([^"]+)" "([^"]+)".*""".r
    val dateFormat = "dd/MMM/yyyy:HH:mm:ss zzzz"
    val df = new SimpleDateFormat(dateFormat)

    env.readTextFile("../access.log")
      .flatMap {
        _.split("\\n") filter {
          _.nonEmpty
        }
      }
      .flatMap {
        _ match {
          case lineRegex(ip, date, target, responseCode, contentSize, referrer, userAgent) =>
            Some(LogLine(ip, df.parse(date), target, responseCode.toInt, contentSize.toInt, referrer, userAgent))
          case _ => None
        }
      }
      .assignAscendingTimestamps(_.date.getTime)
      .map { event => (event.ip, 1) } // only look at IPs
      .keyBy(0) // key by the IP
      .window(EventTimeSessionWindows.withGap(Time.minutes(30))) // Session window with a timeout of 1 day
      .allowedLateness(Time.seconds(5))
      .reduce((w1, w2) => (w1._1, w1._2 + w2._2)) // sum number of entries per IP
      .keyBy(0)
      .sum(1)
      .print()

    env.execute("Flink Scala API Skeleton")
  }
}
