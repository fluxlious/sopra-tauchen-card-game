package service

import gui.Refreshable


abstract class AbstractRefreshingService {
    private val refreshables = mutableListOf<Refreshable?>()


    fun addRefreshable(newRefreshable: Refreshable) {
        refreshables.add(newRefreshable)
    }

    fun onAllRefreshables(method: Refreshable.() -> Unit) {
        refreshables.forEach() {it!!.method()} // Check for "!!"
    }
}
