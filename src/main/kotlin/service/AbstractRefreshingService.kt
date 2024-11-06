package service

import gui.Refreshables


abstract class AbstractRefreshingService {
    private val refreshables = mutableListOf<Refreshables?>()


    fun addRefreshable(newRefreshable: Refreshables) {
        refreshables.add(newRefreshable)
    }

    fun onAllRefreshables(method: Refreshables.() -> Unit) {
        refreshables.forEach() {it!!.method()} // Check for "!!"
    }
}
