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
package org.codefeedr.pipeline

/** Thrown when a pipeline is empty. */
final case class EmptyPipelineException(private val message: String = "",
                                        private val cause: Throwable =
                                          None.orNull)
    extends Exception(message, cause)

/** Thrown when a pipeline has no source. */
final case class NoSourceException(private val message: String = "",
                                   private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

/** Thrown when a pipeline has no sink. */
final case class NoSinkException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

/** Thrown when a stage is not found. */
final case class StageNotFoundException(private val message: String = "",
                                        private val cause: Throwable =
                                          None.orNull)
    extends Exception(message, cause)

/** Thrown when two connected stages have incompatible types. **/
final case class StageTypesIncompatibleException(
    private val message: String = "",
    private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

/** Thrown to list all stages in a pipelines. It's not really an exception, but necessary since Flink has no graceful shutdown.  */
final case class PipelineListException(private val json: String)
    extends Exception(json, null)

/** Thrown when there are conflicting/duplicate stage ids. */
final case class StageIdsNotUniqueException(private val stage: String)
    extends Exception(stage, null)
