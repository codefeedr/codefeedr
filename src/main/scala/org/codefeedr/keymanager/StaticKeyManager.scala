package org.codefeedr.keymanager

class StaticKeyManager(map: Map[String, String] = Map()) extends KeyManager {

  override def request(target: String, numberOfCalls: Int): Option[String]= {
    if (target == null)
      throw new IllegalArgumentException()

    if (numberOfCalls == 0) {
      return Option.empty
    }

    map.get(target)
  }

}
