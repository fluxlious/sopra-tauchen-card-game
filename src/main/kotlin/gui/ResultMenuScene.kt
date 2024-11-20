package gui

import entity.Card
import entity.CardValue
import entity.Player
import service.RootService
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color
import kotlin.system.exitProcess

class ResultMenuScene(private val rootService: RootService) : MenuScene(720, 480), Refreshable {
    val game = rootService.currentGame

    // Headline label at the top center
    private val headlineLabel = Label(
        width = 300,
        height = 50,
        posX = (720 - 300) / 2,
        posY = 50,
        text = "Game Over",
        alignment = Alignment.CENTER,
        font = Font(35, Color(0xFFFFFFF), "Staatliches"),
        visual = ColorVisual(Color( 0x81323B)),
    )

    // Winner label below the headline
    private val winnerLabel = Label(
        text = "",
        width = 600,
        height = 50,
        posX = (720 - 600) / 2,
        posY = 150,
        alignment = Alignment.CENTER,
        font = Font(30, Color(0xFFFFFFF), "Staatliches"),
        visual = ColorVisual(Color(0x0C2027))
    )

    private val restartButton = Button(
        text = "Start New Game",
        width = 280,
        height = 60,
        posX = (720 - 280) / 2, // Center horizontally
        posY = 250, // Below winner label with spacing
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027))
    ).apply {
        onMouseClicked = {
            // Access the onAllRefreshables method of the game service to call the refreshAfterGameRestart method
            rootService.gameService.onAllRefreshables { refreshAfterGameRestart() }
        }
    }
    private val quitButton = Button(
        text = "Quit Game",
        width = 280,
        height = 60,
        posX = (720 - 280) / 2,
        posY = 360,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027))
    ).apply {
        onMouseClicked = {
            exitProcess(0) // Quit the application
        }
    }
    init {
        // Add all components
        addComponents(
            headlineLabel,
            winnerLabel,
            restartButton,
            quitButton,

        )
    }

    // Helper function to format the player's score as a string
    private fun Player.scoreString(): String = "${this.name} scored ${this.score} points."

    override fun refreshAfterEndGame(winner: Player) {
        winnerLabel.text = "${winner.name} Wins!"
    }

    override fun refreshAfterGameRestart() {
        rootService.gameService.startNewGame(listOf("cem", "can"))
    }
}