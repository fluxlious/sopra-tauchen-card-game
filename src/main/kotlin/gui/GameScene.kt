package gui

import entity.Card
import entity.CardSuit
import entity.CardValue
import service.CardImageLoader
import service.RootService
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ColorVisual(Color(0x05b30d))), Refreshable{
    val cardImageLoader : CardImageLoader = CardImageLoader()
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()
    val drawPile: CardView = CardView(
        height = 200,
        width = 130,
        posX = 560,
        posY = 360,
        front = cardImageLoader.backImage
    )
    private val drawPileCount = Label(
        height = 200,
        width = 130,
        posX = 560,
        posY = 360,
        text = "0",
        alignment = Alignment.CENTER,
        font = Font(80, Color(0xE7EFF2), "Staatliches")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.drawCard()
        }
    }


    val discardPile: CardStack<CardView> = CardStack(
        height = 220,
        width = 130,
        posX = 1230,
        posY = 360,
        visual = ColorVisual(255, 255, 255, 50)
    )
    val middleCards : LinearLayout<CardView> = LinearLayout(
        height = 200,
        width = 420,
        posX =  750,
        posY = 360,
        visual = ColorVisual(255, 255, 255, 50)
    )

    var currentPlayerHand = LinearLayout<CardView>(
        height = 220,
        width = 800,
        posX = 560,
        posY = 750,
        spacing = -20,
        visual = ColorVisual(255, 255, 255, 50),
        //alignment = Alignment.CENTER
    )

    val currentPlayerScoringPile = LinearLayout<CardView>(
        height = 200,
        width = 130,
        posX = 1420,
        posY = 750,
        visual = ColorVisual(255, 255, 255, 50)

    )

    var otherPlayerHand: LinearLayout<CardView> = LinearLayout<CardView>(
        height = 220,
        width = 800,
        posX = 560,
        posY = 50,
        spacing = -50,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {
        rotation = 180.0
    }
    val otherPlayerScoringPile: LinearLayout<CardView> = LinearLayout<CardView>(
        height = 200,
        width = 130,
        posX = 370,
        posY = 50,
        visual = ColorVisual(255, 255, 255, 50)

    ).apply {
        rotation = 180.0
    }

    private val currentPlayerName = Label(
        height = 50,
        width = 200,
        posX = 50,
        posY = 750,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )


    init {
        addComponents(
            drawPile,
            drawPileCount,
            discardPile,
            middleCards,
            currentPlayerHand,
            currentPlayerScoringPile,
            currentPlayerName,
            otherPlayerHand,
            otherPlayerScoringPile
        )
    }

    override fun refreshAfterStartNewGame() {
        //create the CardViews for each value and suit combination
        // and add it to BidirectionalMap

        cards.clear()
        CardValue.shortDeck().forEach { value ->
            CardSuit.values().forEach { suit ->
                cards[Card(suit, value)] = CardView(
                    width = 130,
                    height = 200,
                    front = cardImageLoader.frontImageFor(suit,value),
                    back = cardImageLoader.backImage
                )
            }
        }


    }

    override fun refreshAfterStartTurn() {

        // Get the current game from the rootService and return if no game is currently active
        val game = rootService.currentGame ?: return

        currentPlayerName.text = game.players[game.currentPlayerIndex].name

        currentPlayerHand.clear()
        game.players[game.currentPlayerIndex].hand.forEach{ card: Card ->
            currentPlayerHand.add(cards[card] as CardView)
       }

    }
}

