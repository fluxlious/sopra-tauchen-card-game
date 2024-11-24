package gui

import entity.Player
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 *  The class of the main application that all scenes are connected, and manages the scenes.
 *
 */
class SopraApplication : BoardGameApplication("SoPra Game"), Refreshable {

    private val rootService = RootService()


    // Create the game and menu scenes and pass them the root service
    private val gameScene = GameScene(rootService)
    private val mainMenuScene = MainMenuScene(rootService)
    private val resultMenuScene = ResultMenuScene(rootService)


    init {
        // Load the font from resources
        loadFont("Staatliches-Regular.ttf")

        // Connect refreshables to this class and all scenes
        rootService.addRefreshables(
            this,
            mainMenuScene,
            gameScene,
            resultMenuScene
        )

        // Display the main menu scene initially
        this.showMenuScene(mainMenuScene)

    }

    /**
     * This method is called by the [service.GameService] after the game is initialised.
     * It hides the main menu and displays the game scene
     */
    override fun refreshAfterStartNewGame() {
        hideMenuScene(750)
        this.showGameScene(gameScene)

    }

    /**
     * This method is called by the [service.GameService] after the game has ended.
     * It displays the result menu scene.
     *
     * @param winner The [Player] who has won the game
     */
    override fun refreshAfterEndGame(winner: Player) {
        showMenuScene(resultMenuScene)
    }
}

