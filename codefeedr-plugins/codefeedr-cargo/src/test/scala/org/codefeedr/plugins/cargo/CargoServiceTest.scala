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

package org.codefeedr.plugins.cargo

import java.io.InputStream

import scala.io.Source
import org.scalatest.FunSuite

class CargoServiceTest extends FunSuite {

  test ("Parse Cargo.io JSON replies") {
    val stream : InputStream = getClass.getResourceAsStream("/libc.json")
    val libcString : String = Source.fromInputStream(stream).getLines.mkString

    val result = CargoService.parseRespose(libcString)

    assert(result.crate.versions.length == 55)
    assert(result.versions.length == 55)
    assert(result.crate.badges.length == 2)
    assert(result.crate.links.owner_user == "/api/v1/crates/libc/owner_user")
  }

  test ("Parse crate index commit messages") {
    Map("Updating crate `btc-transaction-utils#0.2.0`" -> "btc-transaction-utils",
        "Updating crate `gcollections#1.1.0`" -> "gcollections",
        "Updating crate `sbr#0.1.1`" -> "sbr",
        "Updating crate `error-chain-mini-derive#0.2.0`" -> "error-chain-mini-derive"
    ).foreach(x => assert(CargoService.extactCrateFromCommitMsg(x._1) == x._2))
  }
}
