package gui

import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

class SopraApplication : BoardGameApplication("SoPra Game"), Refreshable {
    private val rootService = RootService()



    // Create the game and menu scenes and pass them the root service
    private val gameScene = GameScene(rootService)
    private val mainMenuScene = MainMenuScene(rootService)


    init {
        rootService.addRefreshables(
            this,
            mainMenuScene,
            //TODO resultMenuScene
            gameScene
        )

        this.showGameScene(gameScene)
        this.showMenuScene(mainMenuScene)
    }

    override fun refreshAfterStartNewGame() {
        hideMenuScene(500)

    }
}

