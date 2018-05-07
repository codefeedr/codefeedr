package org.codefeedr.keymanager

/**
  * A key managed by a key manager.
  *
  * @param value The key
  * @param remainingCalls Number of calls remaining after using the ones requested. Can be 0.
  */
case class ManagedKey(value: String, remainingCalls: Int)

/**
  * A key manager handles the retrieval of API keys for use with, e.g. web APIs.
  *
  * The key manager has multiple targets, each with a set of keys. A key can have
  * a limited amount of calls it can be used for. The key manager handles refreshing
  * of those number of calls.
  */
trait KeyManager {

  /**
    * Request a key for given target with at least the number of calls given remaining.
    *
    * The number of calls will be used to count how many are still remaining.
    *
    * @param target Target of the key,
    * @param numberOfCalls The number of calls needed on this key.
    * @return Managed key
    */
  def request(target: String, numberOfCalls: Int): Option[ManagedKey]

  /**
    * Request single-use key for given target.
    *
    * Convenience method.
    *
    * @param target Target of the key
    * @return Managed key
    */
  def request(target: String): Option[ManagedKey] = request(target, 1)
}