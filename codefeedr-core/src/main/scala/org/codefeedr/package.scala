package org

import org.codefeedr.buffer.{Buffer, BufferFactory}
import org.codefeedr.buffer.serialization.{AbstractSerde, Serializer}

/** Some helper functions to make your life easier **/
package object codefeedr {

  /** Registers a new SerDe. This SerDe needs to be subclassed from [[AbstractSerde]].
    *
    * @param name Name of the SerDe. This needs to be unique. Reserved keywords are: JSON, BSON, KRYO.
    * @param ev Implicit Manifest of the class.
    * @tparam T Type of the serializer.
    * @throws IllegalArgumentException Thrown when name is not unique/already registered.
    */
  def registerSerializer[T <: AbstractSerde[_]](name: String)(
      implicit ev: Manifest[T]) =
    Serializer.register[T](name)

  /** Registers a new Buffer. This Buffer needs to be subclassed from [[Buffer]].
    *
    * @param name Name of the Buffer. This needs to be unique. Reserved keywords are: Kafka, RabbitMQ.
    * @param ev Implicit Manifest of the class.
    * @tparam T Type of the buffer.
    * @throws IllegalArgumentException Thrown when name is not unique/already registered.
    */
  def registerBuffer[T <: Buffer[_]](name: String)(implicit ev: Manifest[T]) =
    BufferFactory.register[T](name)
}
