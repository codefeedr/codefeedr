package org.codefeedr.plugins.ghtorrent.stages

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.{
  DataStream,
  StreamExecutionEnvironment
}
import org.codefeedr.pipeline.Context
import org.codefeedr.plugins.ghtorrent.util.GHTorrentRabbitMQSource
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{any, _}
import org.apache.flink.api.scala._
import org.codefeedr.plugins.ghtorrent.protocol.GHTorrent.Record

class GHTInputStageTest extends FunSuite with MockitoSugar {

  test("Source should properly be added") {
    val mockedEnv = mock[StreamExecutionEnvironment]
    val mockedStream = mock[DataStream[String]]
    val context = Context(env = mockedEnv, "")

    when(
      mockedEnv.addSource(any[GHTorrentRabbitMQSource])(
        any[TypeInformation[String]]))
      .thenReturn(mockedStream)

    val source = new GHTInputStage("")

    source.main(context)

    verify(mockedEnv)
      .addSource(any[GHTorrentRabbitMQSource])(any[TypeInformation[String]])

    verify(mockedStream).map(any[Function1[String, Record]])(
      any[TypeInformation[Record]])

  }

}
