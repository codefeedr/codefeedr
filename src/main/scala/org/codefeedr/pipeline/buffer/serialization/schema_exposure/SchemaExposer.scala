package org.codefeedr.pipeline.buffer.serialization.schema_exposure

import org.apache.avro.Schema

trait SchemaExposer {

  /**
    * Stores a schema bound to a subject.
    * @param schema the schema belonging to that topic.
    * @param subject the subject belonging to that schema.
    * @return true if correctly saved.
    */
  def putSchema(schema : Schema, subject : String) : Boolean

  /**
    * Get a schema based on a subject.
    * @param subject the subject the schema belongs to.
    * @return None if no schema is found or an invalid schema. Otherwise it returns the schema.
    */
  def getSchema(subject : String) : Option[Schema]

  /**
    * Deletes a Schema.
    * @param subject the subject the schema belongs to.
    * @return true if successfully deleted, otherwise false.
    */
  def delSchema(subject : String) : Boolean

  /**
    * Deletes all schemas.
    */
  def deleteAllSchemas()

  /**
    * Tries to parse a String into a Schema.
    * @param schemaString the schema string.
    * @return an option of a Schema.
    */
  def parseSchema(schemaString: String) : Option[Schema] = {
    try {
      val schema = new Schema.Parser().parse(schemaString)
      Some(schema)
    } catch {
      case x : Throwable => None
    }
  }
}
