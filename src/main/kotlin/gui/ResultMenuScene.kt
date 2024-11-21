package gui

import entity.Player
import service.RootService
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import java.awt.Color
import kotlin.system.exitProcess

/**
 * The ResultMenuScene is a MenuScene that displays the winner of the game, the score and the number of trios
 * made by each player.
 *
 * @param rootService The associated [RootService]
*/
class ResultMenuScene(private val rootService: RootService) : MenuScene(720, 480), Refreshable {
    val game = rootService.currentGame

    //This label is used to display "Game Over" headline
    private val headlineLabel = Label(
        width = 300,
        height = 50,
        posX = (720 - 300) / 2,
        posY = 10,
        text = "Game Over",
        alignment = Alignment.CENTER,
        font = Font(35, Color(0xFFFFFFF), "Staatliches"),
        visual = ColorVisual(Color(0x81323B)),
    )
    //This label holds the name of the winner
    private val winnerLabel = Label(
        text = "",
        width = 600,
        height = 50,
        posX = (720 - 600) / 2,
        posY = 80,
        alignment = Alignment.CENTER,
        font = Font(30, Color(0xFFFFFFF), "Staatliches"),
        visual = ColorVisual(Color(0x0C2027))
    )

    //This pane holds the score and the number of trios made by each player
    private val scoreAndTrioPane = Pane<Label>(
        width = 280,
        height = 120,
        posX = (720 - 280) / 2,
        posY = 150,
    )

    //This button restarts the game by calling the associated Refreshable
    private val restartButton = Button(
        text = "Start New Game",
        width = 280,
        height = 60,
        posX = (720 - 280) / 2,
        posY = 250,
        font = Font(22, Color(0xFFFFFFF), "Staatliches"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027))
    ).apply {
        onMouseClicked = {
            // Access the onAllRefreshables method of the game service to call the refreshAfterGameRestart method
            rootService.gameService.onAllRefreshables { refreshAfterGameRestart() }
        }
    }

    // This button ends the application when clicked
    private val quitButton = Button(
        text = "Quit Game",
        width = 280,
        height = 60,
        posX = (720 - 280) / 2,
        posY = 360,
        font = Font(22, Color(0xFFFFFFF), "Staatliches"),
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027))
    ).apply {
        onMouseClicked = {
            exitProcess(0) // Quit the application
        }
    }

    //Initialize the scene with added components
    init {
        // Add all components to the scene
        addComponents(
            headlineLabel,
            winnerLabel,
            restartButton,
            quitButton,
            scoreAndTrioPane,
        )
    }

    /**
     * This method is called by the [service.GameService] after the game ends.
     * It sets the winner name to the [winnerLabel]
     * and adds the score and the number of trios made for each player to the pane.
     *
     * @param winner The [Player] who has won the game
     */
    override fun refreshAfterEndGame(winner: Player) {
        winnerLabel.text = "${winner.name} Wins!"
        val currentGame = rootService.currentGame ?: return

        //Sort the scores of players and then add them to the pane
        currentGame.players.sortedByDescending {it.score}.forEachIndexed{index, player ->
            scoreAndTrioPane.add(
                //Add label for each player with text indicating the points and trios made.
                Label(
                    text = "${player.name}: ${player.score} points and ${player.scoringPile.size} trio(s)",
                    width = 280,
                    height = 30,
                    posX = 0,
                    posY = index * 30, //Each row is 30
                    /*alignment = Alignment.TOP_LEFT,*/
                    font = Font(20, Color(0xFFFFFFF), "Staatliches"),
                    visual = ColorVisual(Color(0x0C2027))
                )
            )

        }

    }

    override fun refreshAfterGameRestart() {
        val game = rootService.currentGame ?: return
        rootService.gameService.startNewGame(game.players.map { it.name })
    }
}