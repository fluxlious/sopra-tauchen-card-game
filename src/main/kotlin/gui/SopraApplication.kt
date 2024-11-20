package gui

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.dialog.Dialog

class SopraApplication : BoardGameApplication("SoPra Game"), Refreshable {
    private val rootService = RootService()


    // Create the game and menu scenes and pass them the root service
    private val gameScene = GameScene(rootService)
    private val mainMenuScene = MainMenuScene(rootService)
    private val resultMenuScene = ResultMenuScene(rootService)


    init {
        rootService.addRefreshables(
            this,
            mainMenuScene,
            gameScene,
            resultMenuScene
        )
        this.showMenuScene(mainMenuScene)

    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(750)
        this.showGameScene(gameScene)


    }

    override fun refreshAfterEndGame(winner: Player) {
        showMenuScene(resultMenuScene)
    }
}

