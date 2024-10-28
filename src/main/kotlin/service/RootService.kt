package service

import entity.Tauchen

class RootService {
    val GameService = GameService(this)
    val PlayerActionService = PlayerActionService(this)
}