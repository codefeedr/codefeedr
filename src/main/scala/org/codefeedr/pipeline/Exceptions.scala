package org.codefeedr.pipeline

final case class EmptyPipelineException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class NoSourceException(private val message: String = "",
                                   private val cause: Throwable = None.orNull)
  extends Exception(message, cause)

final case class NoSinkException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
