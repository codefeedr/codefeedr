package org.codefeedr.plugins.pypi.util

import java.text.SimpleDateFormat

import org.codefeedr.plugins.pypi.protocol.Protocol.PyPiProject
import org.codefeedr.stages.utilities.HttpRequester
import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTimeSerializers
import scalaj.http.{Http, HttpRequest}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.Extraction._
import org.json4s.JsonAST._

object PyPiService extends Serializable {

  lazy implicit val formats: Formats = new DefaultFormats {
    override def dateFormatter: SimpleDateFormat =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  } ++ JavaTimeSerializers.all
  private val url = "https://pypi.org/pypi/"

  /** Retrieves a PyPi project.
    *
    * @param projectName the name of the project.
    * @return an optional PyPiProject.
    */
  def getProject(projectName: String): Option[PyPiProject] = {
    val projectEndPoint = projectName + "/json"

    val rawProject = getProjectRaw(projectEndPoint)

    if (rawProject.isEmpty) return None

    val json = parse(rawProject.get)
    // println(json)
    //println(Some(extract[PyPiProject](transformProject(json))))
    if (extractOpt[PyPiProject](transformProject(json)).isEmpty) {
      println(json)
    }

    extractOpt[PyPiProject](transformProject(json))
  }

  def transformProject(json: JValue): JValue =
    json transformField {
      case JField("releases", JObject(x)) => {
        val newList = x.map { y =>
          new JObject(
            List(JField("version", JString(y._1)), JField("releases", y._2)))
        }

        JField("releases", JArray(newList))
      }
    }

  /** Returns a project as a raw string.
    *
    * @param endpoint the end_point to do the request.
    * @return an optional String.
    */
  def getProjectRaw(endpoint: String): Option[String] = {
    val response = try {
      val request = Http(url + endpoint).headers(getHeaders)
      new HttpRequester().retrieveResponse(request)
    } catch {
      case _: Throwable => return None
    }

    Some(response.body)
  }

  /** Add a user-agent with contact details. */
  def getHeaders: List[(String, String)] =
    ("User-Agent", "CodeFeedr-PyPi/1.0 Contact: zorgdragerw@gmail.com") :: Nil

}
