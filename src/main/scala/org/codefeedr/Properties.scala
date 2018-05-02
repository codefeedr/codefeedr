package org.codefeedr

import java.util
import collection.JavaConverters._

/**
  * Object containing configuration properties.
  */
class Properties {
  protected var contents = new util.Properties()

  def get(key: String, default: String = ""): String = {
    val value = contents.getProperty(key)

    if (value == null)
      return default

    value
  }

  def set(key: String, value: String): Unit = {
    contents.setProperty(key, value)
  }

  def keys(): List[String] = {
    contents.keys().asScala.toList.asInstanceOf[List[String]]
  }

  override def equals(that: Any): Boolean = that match {
    case that: Properties => that.contents == contents
    case _ => false
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
  */
final class ImmutableProperties extends Properties {

  def this(properties: util.Properties) {
    this()

    contents = properties
  }

  def this(properties: Properties) {
    this()

    contents = properties.toJavaProperties
  }

  override def set(key: String, value: String): Unit = {
    throw new NotImplementedError("set() not allowed on an immutable object")
  }

  override def toImmutable: ImmutableProperties = this
}