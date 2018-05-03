package org.codefeedr.keymanager

case class ManagedKey(value: String, remainingCalls: Int)

trait KeyManager {

  def request(target: String, numberOfCalls: Int): Option[ManagedKey]

}