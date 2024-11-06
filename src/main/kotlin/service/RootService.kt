package service

import entity.Tauchen
import gui.Refreshable

class RootService {
    val gameService = GameService(this)
    val playerActionService = PlayerActionService(this)
    var currentGame: Tauchen? = null

    fun addRefreshable(newRefreshable: Refreshable) {
        gameService.addRefreshable(newRefreshable)          // Check !
//        playerActionService.addRefreshable(newRefreshable)  // Check !
    }
    fun addRefreshables(vararg newRefreshables: Refreshable) {
        newRefreshables.forEach { addRefreshable(it) }
    }




}