package org.codefeedr.keymanager.redis

import org.codefeedr.keymanager.KeyManager

class ScalaKeyManager extends KeyManager {
  override def request(target: String, numberOfCalls: Int): Option[String] = ???
}
