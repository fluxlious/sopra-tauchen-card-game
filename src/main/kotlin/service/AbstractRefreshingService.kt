package service

import gui.Refreshable

/**
 * Abstract service class that handles multiples [Refreshable]
 * by notifying all of them when certain actions occur, using the [onAllRefreshables] method.
 */
abstract class AbstractRefreshingService {
    private val refreshables = mutableListOf<Refreshable?>()

    /**
     * adds a new Refreshable to the list that gets notified whenever [onAllRefreshables] is used
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables.add(newRefreshable)
    }
    /**
     * Adds each of the provided [Refreshable]s to the mutable list of refreshables.
     *
     * @param method The [Refreshable]s to be added
     */
    fun onAllRefreshables(method: Refreshable.() -> Unit) =
        refreshables.forEach { it?.method() }

}
