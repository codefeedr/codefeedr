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
package org.codefeedr.pipeline.buffer.serialization

import java.io.ByteArrayOutputStream

import org.apache.avro.io.{BinaryEncoder, DatumWriter, EncoderFactory}
import org.apache.avro.reflect.ReflectDatumWriter
import org.apache.avro.specific.{SpecificDatumWriter, SpecificRecordBase}
import org.apache.flink.api.common.serialization.SerializationSchema

import scala.reflect.{ClassTag, classTag}

class AvroSerializationSchema[In : ClassTag] extends SerializationSchema[In] {

  //Get type of the class at run time
  val inputClassType : Class[In] = classTag[In].runtimeClass.asInstanceOf[Class[In]]

  override def serialize(element: In): Array[Byte] = {
    val out : ByteArrayOutputStream  = new ByteArrayOutputStream()
    val encoder : BinaryEncoder = EncoderFactory.get().binaryEncoder(out, null)
    var writer : DatumWriter[In] = null

    if (classOf[SpecificRecordBase].isAssignableFrom(inputClassType)) {
      writer = new SpecificDatumWriter[In](inputClassType)
    } else {
      writer = new ReflectDatumWriter[In](inputClassType)
    }

    writer.write(element, encoder)
    encoder.flush()
    out.close()

    out.toByteArray
  }
}