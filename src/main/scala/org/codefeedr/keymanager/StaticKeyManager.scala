package org.codefeedr.keymanager

class StaticKeyManager(map: Map[String, String] = Map()) extends KeyManager {

  override def request(target: String, numberOfCalls: Int): Option[(String, Int)]= {
    if (target == null)
      throw new IllegalArgumentException()

    if (numberOfCalls == 0) {
      return Option.empty
    }

    val key = map.get(target)
    if (key.isEmpty)
      None
    else
      Some((key.get, Int.MaxValue))
  }

}
