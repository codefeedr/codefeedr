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
package org.codefeedr.pipeline.buffer.serialization.schema_exposure

import org.apache.avro.Schema
import org.scalatest.{BeforeAndAfter, FunSuite}

case class Person(name : String, age : Int)

abstract class SchemaExposerTest extends FunSuite with BeforeAndAfter {

  val schema = """{
                   "type": "record",
                   "name": "User",
                   "fields": [
                     {
                         "name": "name",
                         "type": "string"
                     },
                     {
                         "name": "age",
                         "type": "int"
                     }
                   ]
                 }"""

  var exposer : SchemaExposer = _
  var parsedSchema : Schema = _
  val subject = "testSubject"

  def getSchemaExposer() : SchemaExposer

  before {
    exposer = getSchemaExposer()
    parsedSchema = exposer
      .parseSchema(schema)
      .get
  }

  after {
    exposer.deleteAllSchemas()
  }

  test("A simple schema should be correctly saved") {
    //put the schema
    exposer.putSchema(parsedSchema, subject)

    //if I get the schema, it should be the same
    assert(exposer.getSchema(subject).get == parsedSchema)
  }

  test("A simple schema should be correctly deleted") {
    //put the schema
    assert(exposer.putSchema(parsedSchema, subject))

    //it should be properly deleted
    assert(exposer.delSchema(subject))
  }

  test("A simple schema cannot be deleted if it is not there") {
    //it should be properly deleted
    assert(!exposer.delSchema(subject))
  }

  test("Get a schema on a non existent subject should return None") {
    assert(exposer.getSchema("IDoNoTeXiSt") == None)
  }

  test("An invalid schema should return None") {
    assert(exposer.parseSchema("iNVaLIdScHemA{}$%:)") == None)
  }


}
