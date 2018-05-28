package org.codefeedr.plugins.travis.util

import org.codefeedr.keymanager.KeyManager
import org.codefeedr.plugins.travis.TravisProtocol.{TravisBuild, TravisBuilds, TravisRepository}
import org.codefeedr.plugins.travis.util.TravisExceptions.{CouldNotExtractException, CouldNotGetResourceException}
import org.json4s.ext.JavaTimeSerializers
import org.json4s.jackson.JsonMethods.parse
import org.json4s.{DefaultFormats, Formats, JValue}
import scalaj.http.Http

/**
  * Used to interact with the travis API
  * @param keyManager KeyManager that contains keys for the travis API
  */
class TravisService(keyManager: KeyManager) extends Serializable {

  lazy implicit val formats: Formats = DefaultFormats ++ JavaTimeSerializers.all

  private val url = "https://api.travis-ci.org"

  /**
    * Returns a function that checks if a given repo is active on Travis.
    * @return True if it is active, false otherwise
    */
  def repoIsActiveFilter: String => Boolean = {
    val filter: String => Boolean = slug => {
      val responseBody = getTravisResource("/repo/" + slug.replace("/", "%2F"))
      val isActive = try {
        val active = parse(responseBody).extract[TravisRepository].active.getOrElse(false)
        active
      } catch {
        case _: org.json4s.MappingException =>
          false
      }
      isActive
    }
    filter
  }

  /**
    * Gives a specific page with build information of a give repository on a given branch.
    * @param owner Name of the owner of the repository
    * @param repoName Name of the repository
    * @param branch Name of the branch
    * @param offset How many builds to skip
    * @param limit How many builds to get
    * @return Page with Travis builds
    */
  def getTravisBuilds(owner: String, repoName: String, branch: String = "", offset: Int = 0, limit: Int = 25): TravisBuilds = {
    getTravisBuildsWithSlug(owner + "%2F" + repoName, branch, offset, limit)
  }

  /**
    * Gives a specific page with build information of a give repository on a given branch.
    * @param slug Slug of the repository
    * @param branch Name of the branch
    * @param offset How many builds to skip
    * @param limit How many builds to get
    * @return Page with Travis builds
    */
  def getTravisBuildsWithSlug(slug: String, branch: String = "", offset: Int = 0, limit: Int = 25): TravisBuilds = {

    val url = "/repo/" + slug + "/builds" +
      (if (branch.nonEmpty) "?branch.name=" + branch else "?") +
      "&sort_by=started_at:desc" +
      "&offset=" + offset +
      "&limit=" + limit

    val responseBody = getTravisResource(url)
    val json = parse(responseBody)
    val builds = extract[TravisBuilds](json)
    builds
  }

  /**
    * Requests a build from Travis with a specific id
    * @param buildID Id of the build
    * @return Travis build
    */
  def getBuild(buildID: Int): TravisBuild = {
    val url = "/build/" + buildID

    val responseBody = getTravisResource(url)
    val json = parse(responseBody)

    val build = extract[TravisBuild](json)
    build
  }

  /**
    * Extracts a case class from a Jvalue and throws an exception if it fails
    * @param json JSon from which a case class should be extracted
    * @tparam A Case class
    * @return Extracted JSon in case class
    */
  def extract[A : Manifest](json: JValue): A = {
    try {
      json.extract[A]
    } catch {
      case _: Throwable =>
        throw CouldNotExtractException("Could not extract case class from JValue: " + json)
    }
  }

  /**
    * Gets the response body from a specified endpoint in the travis API
    * @param endpoint Endpoint
    * @return Body of the Http response
    */
  @throws(classOf[CouldNotGetResourceException])
  def getTravisResource(endpoint: String): String = {
    try {
      Http(url + endpoint)
        .headers(getHeaders)
        .asString.body
    } catch {
      case _: Throwable =>
        throw CouldNotGetResourceException("Could not get the requested resource from: " + url + endpoint)
    }
  }

  /**
    * Gets the needed headers for travis API requests
    * @return
    */
  def getHeaders: List[(String, String)] = {
    ("Travis-API-Version", "3") ::
      ("Authorization", "token " + keyManager.request("travis").get.value) ::
      Nil

  }
}


