package org.codefeedr.buffer.serialization

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}
import org.codefeedr.pipeline.PipelineItem
import org.json4s.NoTypeHints
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.Serialization
import shapeless.{HList, LabelledGeneric}
import shapeless.datatype.avro.{AvroSchema, AvroType, FromAvroRecord, ToAvroRecord}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._
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
trait AvroSerde[T] extends AbstractSerde[T] {
  type L <: HList
  def setSchema(str : String)
}

object AvroSerde {
  type Aux[T, L0 <: HList] = AvroSerde[T] { type L = L0 }

  def apply[T](implicit serializer: AvroSerde[T]): AvroSerde[T] = serializer

  implicit def mkSerializer[T : ClassTag : TypeTag, L0 <: HList](implicit
                                                                 gen: LabelledGeneric.Aux[T, L0],
                                                                 toL: ToAvroRecord[L0],
                                                                 fromL: FromAvroRecord[L0]): Aux[T, L0] =
    new AvroSerde[T] {
      val date = AvroType.at[LocalDateTime](Schema.Type.STRING)(x => LocalDateTime.parse(x.toString), _.toString)

      type L = L0

      //work around for serialization
      var schemaString = ""
      var schemaSet = false

      @transient
      lazy val exposedSchema : Schema = new Schema.Parser().parse(schemaString)

      //Get Avro Type
      val avroType = AvroType[T]

      override def serialize(value : T) : Array[Byte] = {
        val r = avroType.toGenericRecord(value)
        val writer = new GenericDatumWriter[GenericRecord](r.getSchema)
        val baos = new ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(baos, null)
        writer.write(r, encoder)
        encoder.flush()
        baos.close()

        baos.toByteArray
      }

      override def deserialize(message: Array[Byte]) : T = {
        val schema = AvroSchema[T]

        val reader = new GenericDatumReader[GenericRecord](schema)
        val bais = new ByteArrayInputStream(message)
        val decoder = DecoderFactory.get().binaryDecoder(bais, null)

        avroType.fromGenericRecord(reader.read(null, decoder)).get
      }

      def setSchema(schema : String) = {
        this.schemaString = schema
        this.schemaSet = true
      }
    }
}
