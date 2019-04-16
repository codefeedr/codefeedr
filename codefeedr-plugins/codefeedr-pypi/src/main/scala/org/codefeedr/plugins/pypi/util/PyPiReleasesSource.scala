package org.codefeedr.plugins.pypi.util

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiRelease

class PyPiReleasesSource extends SourceFunction[PyPiRelease] {

  override def run(ctx: SourceFunction.SourceContext[PyPiRelease]): Unit = ???

  override def cancel(): Unit = ???
}
