package org.codefeedr.plugins.weblogs

import org.apache.flink.streaming.api.scala._
import org.codefeedr.pipeline._
import org.codefeedr.buffer.BufferType
import org.codefeedr.stages.utilities.StringType
import org.codefeedr.testUtils.StringCollectSink
import org.scalatest.{BeforeAndAfter, FunSuite}

class HttpdLogInputStageTest extends FunSuite with BeforeAndAfter {

  test("LogSource integration test") {

    new PipelineBuilder()
      .setBufferType(BufferType.None)
      .append(new HttpdLogInputStage(getClass.getResource("/access.log").getPath))
      .append(new MyPipelineObject)
      .append { x: DataStream[StringType] =>
        x.addSink(new StringCollectSink)
      }
      .build()
      .startMock()

    val res = StringCollectSink.result

    assert(res.contains("HttpdLogItem(46.72.177.4,2015-12-12T18:31:08,POST,/administrator/index.php,HTTP/1.1,200,4494,\"http://almhuette-raith.at/administrator/\",\"Mozilla/5.0 (Windows NT 6.0; rv:34.0) Gecko/20100101 Firefox/34.0\")"))

  }
}

class MyPipelineObject extends PipelineObject[HttpdLogItem, StringType] {
  override def transform(source: DataStream[HttpdLogItem]): DataStream[StringType] = {
    source.map(x => StringType(x.toString))
  }
}

