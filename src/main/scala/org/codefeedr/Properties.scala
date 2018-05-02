package org.codefeedr

import java.util
import collection.JavaConverters._

/**
  * Object containing configuration properties.
  */
class Properties {
  protected val contents = new util.Properties()

  def get(key: String): String = {
    contents.getProperty(key)
  }

  def set(key: String, value: String): Unit = {
    contents.setProperty(key, value)
  }

  def keys(): List[String] = {
    contents.keys().asScala.toList.asInstanceOf[List[String]]
  }

  /**
    * Acquire a Java Properties object containing the same properties as this object.
    *
    * @note Some Flink internal systems use Properties. Only use this for those.
    *
    * @return a Java Properties object
    */
  def toJavaProperties: util.Properties = contents

  /**
    * Create an immutable version of these properties.
    *
    * @return Immutable version
    */
  def toImmutable: ImmutableProperties = new ImmutableProperties(contents)
}

/**
  * Immutable version of Properties. Create using properties.toImmutable
  *
  * @param properties
  */
final class ImmutableProperties(properties: util.Properties) extends Properties {

  def this(properties: Properties) {
    this(properties.toJavaProperties)
  }

  override def set(key: String, value: String): Unit = {
    throw new NotImplementedError("set() not allowed on an immutable object")
  }

  override def toImmutable: ImmutableProperties = this
}