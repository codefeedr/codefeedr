package org.codefeedr.pipeline

/**
  *
  * @param message
  * @param cause
  */
final case class EmptyPipelineException(private val message: String = "",
                                        private val cause: Throwable = None.orNull)
  extends Exception(message, cause)
