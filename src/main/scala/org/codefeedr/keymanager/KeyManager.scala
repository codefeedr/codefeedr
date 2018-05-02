package org.codefeedr.keymanager

trait KeyManager {

  def request(target: String, numberOfCalls: Int): Option[String]

}