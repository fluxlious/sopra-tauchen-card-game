package service

import entity.Tauchen
import gui.Refreshable
/**
 * This base service connects the services to each other and allows access.
 *
 * @property gameService The connected [GameService]
 * @property playerActionService The connected [PlayerActionService]
 * @property currentGame The currently active [entity.Tauchen]. Can be `null`, if no game has started yet.
 */

class RootService {
    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    var currentGame: Tauchen? = null

    /**
     * Adds the [newRefreshable] to all services connected to this [RootService]
     *
     * @param [newRefreshable] the refreshable to be added to this [RootService]
     */
    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)
        playerActionService.addRefreshable(newRefreshable)
    }

    /**
     * Adds each of the [newRefreshables] to all services
     * connected to this root service
     *
     * @param newRefreshables The [Refreshable]s to be added
     */
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }
}