package gui

import entity.Card
import entity.CardSuit
import entity.CardValue
import service.CardImageLoader
import service.RootService
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import java.awt.Color

class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ImageVisual("background.jpg")), Refreshable {
    val cardImageLoader: CardImageLoader = CardImageLoader()
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()
    val drawPile: CardView = CardView(
        height = 250,
        width = 162.0,
        posX = 370,
        posY = 425,
        front = cardImageLoader.backImage
    )
    private val drawPileCount = Label(
        height = 250,
        width = 162.0,
        posX = 370,
        posY = 425,
        text = "0",//inital value
        alignment = Alignment.CENTER,
        font = Font(80, Color(0xE7EFF2), "Staatliches")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.drawCard()
        }
    }


    val discardPile: CardStack<CardView> = CardStack(
        height = 250,
        width = 162.0,
        posX = 1230,
        posY = 425,
        visual = ColorVisual(255, 255, 255, 50)
    )
    val middleCards: LinearLayout<CardView> = LinearLayout(
        height = 220,
        width = 420,
        posX = 750,
        posY = 360,
        spacing = 30,
        visual = ColorVisual(255, 255, 255, 50)
    )

    var currentPlayerHand = LinearLayout<CardView>(
        height = 220,
        width = 800,
        posX = 560,
        posY = 750,
        spacing = -30,
        //visual = ColorVisual(255, 255, 255, 50),
        //alignment = Alignment.CENTER
    )

    var currentPlayerScoringPile = LinearLayout<CardView>(
        height = 250,
        width = 162.0,
        posX = 1420,
        posY = 750,
        visual = ColorVisual(255, 255, 255, 50)

    )
    val nextTurn = Button(
        height = 200,
        width = 200,
        posX = 50,
        posY = 540,
        text = "Next turn",
        alignment = Alignment.CENTER,

    ).apply {
        onMouseClicked = {
            rootService.gameService.endTurn()
        }

    }
    var otherPlayerHand = LinearLayout<CardView>(
        height = 220,
        width = 800,
        posX = 560,
        posY = 140,
        spacing = -30,
        //alignment = Alignment.CENTER,
        //visual = ColorVisual(255, 255, 255, 50)
    ).apply {
        rotation = 180.0
    }

    var otherPlayerScoringPile: LinearLayout<CardView> = LinearLayout<CardView>(
        height = 250,
        width = 162.0,
        posX = 370,
        posY = 50,
        visual = ColorVisual(255, 255, 255, 50)

    ).apply {
        rotation = 180.0
    }

    //player name labels
    private val currentPlayerName = Label(
        height = 50,
        width = 200,
        posX = 750,
        posY = 650,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "JetBrains Mono ExtraBold")
    )
    private val otherPlayerName = Label(
        height = 50,
        width = 200,
        posX = 750,
        posY = 50,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "A")


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
            otherPlayerScoringPile,
            otherPlayerName,
            nextTurn
        )
    }

    override fun refreshAfterStartNewGame() {
        //create the CardViews for each value and suit combination
        // and add it to BidirectionalMap
        val game = rootService.currentGame ?: return

        cards.clear()
        CardValue.values().forEach { value ->
            CardSuit.values().forEach { suit ->
                cards[Card(suit, value)] = CardView(
                    width = 130,
                    height = 200,
                    front = cardImageLoader.frontImageFor(suit, value),
                    back = cardImageLoader.backImage
                )
            }
            //Show the number of cards in the drawPile
            drawPileCount.text = game.drawPile.size.toString()


        }
    }
        override fun refreshAfterStartTurn() {

            // Get the current game from the rootService and return if no game is currently active
            val game = rootService.currentGame ?: return

            //change the current players label
            currentPlayerName.text = game.players[game.currentPlayerIndex].name

            //get the index of other player with modulo
            val otherPlayer = game.players[(game.currentPlayerIndex +1) % 2]
            otherPlayerHand.clear()

            currentPlayerHand.clear()
            currentPlayerScoringPile.clear()
            game.players[game.currentPlayerIndex].hand.forEach { card ->
                currentPlayerHand.add(
                    (cards[card] as CardView).apply {
                        applyHoverEffect(this)
                    }
                )
            }
            //show back cards on the otherPlayer area
                otherPlayer.hand.forEach { card ->
                otherPlayerHand.add(
                    (cards[card] as CardView).apply {
                        removeHoverEffect(this)
                    }
                )
            }

        }

    override fun refreshAfterDrawCard(drawnCard: Card) {
            currentPlayerHand.add(
                (cards[drawnCard] as CardView).apply {
                    applyHoverEffect(this)
                }
            )
    }


        private fun applyHoverEffect(cardView: CardView) {
            cardView.onMouseEntered = {
                cardView.posY -= 25
            }
            cardView.onMouseExited = {
                cardView.posY += 25
            }
            cardView.width = 162.0
            cardView.height = 250.0
            cardView.rotation = 0.0
            cardView.showFront()
            cardView.isDraggable = true
        }

    private fun removeHoverEffect(cardView: CardView) {
        cardView.onMouseEntered = null
        cardView.onMouseExited = null
        cardView.width = 162.0
        cardView.height = 250.0
        cardView.rotation = 0.0
        cardView.showBack()
        cardView.isDraggable = false
    }



}


