package org.codefeedr.buffer.serialization.schema_exposure

trait SchemaExposer {

  /**
    * Stores a schema bound to a subject.
    *
    * @param schema the schema belonging to that topic.
    * @param subject the subject belonging to that schema.
    * @return true if correctly saved.
    */
  def put(schema : AnyRef, subject : String) : Boolean

  /**
    * Get a schema based on a subject.
    *
    * @param subject the subject the schema belongs to.
    * @return None if no schema is found or an invalid schema. Otherwise it returns the schema.
    */
  def get(subject : String) : Option[AnyRef]

  /**
    * Deletes a Schema.
    *
    * @param subject the subject the schema belongs to.
    * @return true if successfully deleted, otherwise false.
    */
  def delete(subject : String) : Boolean

  /**
    * Deletes all schemas.
    */
  def deleteAll()

  /**
    * Tries to parse a String into a Schema.
    *
    * @param schemaString the schema string.
    * @return an option of a Schema.
    */
  def parse(schemaString: String) : Option[AnyRef] = {
//    try {
//      val schema = new Schema.Parser().parse(schemaString)
//      Some(schema)
//    } catch {
//      case x : Throwable => None
//    }
     None
  }
}
