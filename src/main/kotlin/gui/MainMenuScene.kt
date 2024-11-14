package gui

import service.RootService
import tools.aqua.bgw.components.uicomponents.*

import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.*
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color
import kotlin.system.exitProcess

class MainMenuScene(rootService: RootService) : MenuScene(width = 1920, height = 1080, background = ImageVisual("background.png")) {

    val newGameButton: Button = Button(
        height = 150,
        width = 400,
        posX = 550,
        posY = 700,
        text = "",
        font = Font(color = Color.WHITE, fontStyle = Font.FontStyle.ITALIC),
        visual = ImageVisual("play.png")
    ).apply {
        onMouseClicked = {
            rootService.gameService.startNewGame(playerNames = listOf("Cem","Can"))
        }
    }


    val quitButton: Button = Button(
        height = 150,
        width = 400,
        posX =  1000,
        posY =  700,
        text = "",
        font = Font(color = Color.WHITE, fontStyle = Font.FontStyle.ITALIC),
        visual = ImageVisual("quit.png")
    ).apply { onMouseClicked = {
                    exitProcess(0)
    }
}

    private val p1Label = Label(
        width = 200, height = 80,
        posX = 610,
        posY = 350,
        text = "Player 1:",
        font = Font(42)
    )
    private val p1Input: TextField = TextField(
        width = 400, height = 80,
        posX = 820,
        posY = 350,
        text = listOf("Homer", "Marge", "Bart", "Lisa", "Maggie").random(),
        font = Font(42)

    ).apply {
        onKeyTyped = {
            newGameButton.isDisabled = this.text.isBlank() || p2Input.text.isBlank()
        }
    }

    private val p2Label = Label(
        width = 200, height = 80,
        posX = 610,
        posY = 450,
        text = "Player 2:",
        font = Font(42)
    )

    private val p2Input: TextField = TextField(
        width = 400, height = 80,
        posX = 820,
        posY = 450,
        text = listOf("Fry", "Bender", "Leela", "Amy", "Zoidberg").random(),
        font = Font(42)
    ).apply {
        onKeyTyped = {
            newGameButton.isDisabled = p1Input.text.isBlank() || this.text.isBlank()
        }
    }



    private val menuLabel: Label = Label(
        height = 100,
        width = 200,
        posX = 50,
        posY = 0,
        text = "",
        font = Font(fontWeight = Font.FontWeight.BOLD)
    )

    init {
        addComponents(
            menuLabel,
            newGameButton,
            quitButton,
            p1Label, p1Input,
            p2Label, p2Input

        )
    }
}

