package service

import entity.Tauchen
import gui.Refreshables

class RootService {
    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    var currentGame: Tauchen? = null

    fun addRefreshable(newRefreshable: Refreshables) {
        gameService.addRefreshable(newRefreshable)          // Check !
        playerActionService.addRefreshable(newRefreshable)  // Check !
    }
    fun addRefreshables(vararg newRefreshables: Refreshables) {
        newRefreshables.forEach { addRefreshable(it) }
    }




}