package gui

import service.RootService
import tools.aqua.bgw.components.uicomponents.*

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.*
import tools.aqua.bgw.visual.ImageVisual

import kotlin.system.exitProcess

class MainMenuScene(rootService: RootService) : MenuScene(width = 1920, height = 1080, background = ImageVisual("background.png")), Refreshable {

    private val newGameButton: Button = Button(
        height = 150,
        width = 350,
        posX = 550,
        posY = 700,
        text = "",
        visual = ImageVisual("play.png")
    ).apply {
        onMouseClicked = {
            val playerNames = mutableListOf(player1Input.text, player2Input.text)
            rootService.gameService.startNewGame(playerNames)
        }
    }

    private val quitButton: Button = Button(
        height = 150,
        width = 350,
        posX =  1000,
        posY =  700,
        text = "",
        visual = ImageVisual("quit.png")
    ).apply { onMouseClicked = {
        exitProcess(0)
    }
}

    private val player1Label = Label(
        width = 200, height = 80,
        posX = 610,
        posY = 350,
        text = "Player 1:",
        font = Font(42)
    )

    private val player1Input: TextField = TextField(
        width = 400, height = 80,
        posX = 820,
        posY = 350,
        text = listOf("Homer", "Marge", "Bart", "Lisa", "Maggie").random(),
        font = Font(42)

    ).apply {
        onKeyTyped = {
            newGameButton.isDisabled = this.text.isBlank() || player2Input.text.isBlank()
        }
    }

    private val player2Label = Label(
        width = 200, height = 80,
        posX = 610,
        posY = 450,
        text = "Player 2:",
        font = Font(42)
    )

    private val player2Input: TextField = TextField(
        width = 400, height = 80,
        posX = 820,
        posY = 450,
        text = listOf("Fry", "Bender", "Leela", "Amy", "Zoidberg").random(),
        font = Font(42)
    ).apply {
        onKeyTyped = {
            newGameButton.isDisabled = player1Input.text.isBlank() || this.text.isBlank()
        }
    }


    init {
        addComponents(
            newGameButton,
            quitButton,
            player1Label, player1Input,
            player2Label, player2Input

        )
    }
}

