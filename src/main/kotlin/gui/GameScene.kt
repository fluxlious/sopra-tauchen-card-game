package gui

import entity.Card
import entity.CardSuit
import entity.CardValue
import entity.Player
import service.CardImageLoader
import service.RootService
import tools.aqua.bgw.animation.MovementAnimation

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
import tools.aqua.bgw.dialog.Dialog
import tools.aqua.bgw.dialog.DialogType

class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ImageVisual("game_background4.png")), Refreshable {

    private val cardImageLoader: CardImageLoader = CardImageLoader()
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()

    private var drawPile = CardStack<CardView>(
        height = 231,
        width = 150,
        posX = 370,
        posY = 425,
        visual = cardImageLoader.backImage,
    )

    private val drawPileCount = Label(
        height = 231,
        width = 150,
        posX = 370,
        posY = 425,
        text = "0",//initial value
        alignment = Alignment.CENTER,
        font = Font(80, Color(0xE7EFF2), "Staatliches")
    ).apply {
        onMouseClicked = {
            rootService.playerActionService.drawCard()
        }
    }

    val discardPile: CardStack<CardView> = CardStack<CardView>(
        height = 251,
        width = 170,
        posX = 1420,
        posY = 425,
        visual = ColorVisual(255, 255, 255, 50),
        alignment = Alignment.CENTER,
    ).apply {
        dropAcceptor = { dragEvent ->
            val draggedComponent = dragEvent.draggedComponent
            val game = rootService.currentGame
            checkNotNull(game)
            //accept the dragging if the player has more than 8 cards in the hand
            if (draggedComponent is CardView) {
                game.players[game.currentPlayerIndex].hand.size > 8
            } else {
                false
            }
        }
        onDragDropped = {dragEvent ->
            val cardView = dragEvent.draggedComponent as CardView
            val card = cards.backward(cardView)
            rootService.playerActionService.discardCard(card)
        }
    }

    val middleCards = LinearLayout<CardView>(
        height = 251,
        width = 420,
        posX = 750,
        posY = 425,
        spacing = 30,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {
        //needs boolean
        dropAcceptor = { dragEvent ->
            val draggedComponent = dragEvent.draggedComponent
            val game = rootService.currentGame
            checkNotNull(game)
            val currentPlayer = game.players[game.currentPlayerIndex]


            // Ensure the dragged component is a CardView and belongs to the current player's hand
            if (draggedComponent is CardView) {
                val card = cards.backward(draggedComponent)


                rootService.playerActionService.isCardValid(card) &&
                        (card == currentPlayer.lastDrawnCard || !currentPlayer.hasActionTaken)
            } else {
                false
            }
        }
        onDragDropped = { dragEvent ->
            val cardView = dragEvent.draggedComponent as CardView
            val card = cards.backward(cardView)

            // Play the card
            rootService.playerActionService.playCard(card)

        }
    }


    private var currentPlayerHand = LinearLayout<CardView>(
        height = 251,
        width = 800,
        posX = 560,
        posY = 750,
        spacing = -30,
        visual = ColorVisual(255, 255, 255, 50),
        alignment = Alignment.CENTER
    )

    var otherPlayerHand = LinearLayout<CardView>(
        height = 251,
        width = 800,
        posX = 560,
        posY = 50,
        spacing = -30,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {
        rotation = 180.0
    }

    var currentPlayerScoringPile = LinearLayout<CardView>(
        height = 251,
        width = 300,
        posX = 1420,
        posY = 750,
        spacing = -60,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )
    var otherPlayerScoringPile: LinearLayout<CardView> = LinearLayout<CardView>(
        height = 251,
        width = 300,
        posX = 1420,
        posY = 50,
        spacing = -60,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    ).apply {
        rotation = 180.0
    }
    var currentPlayerScore = Label(
        height = 50,
        width = 170,
        posX = 1420,
        posY = 1002,
        text = "Score: 0",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")
    )
    var otherPlayerScore = Label(
        height = 50,
        width = 170,
        posX = 1420,
        posY = 0,
        text = "Score: 0",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")
    )


    //player name labels
    private val currentPlayerName = Label(
        height = 50,
        width = 200,
        posX = 560,
        posY = 1002,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")

    )

    private val otherPlayerName = Label(
        height = 50,
        width = 200,
        posX = 560,
        posY = 0,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")

    )
    val nextTurn = Button(
        height = 251,
        width = 150,
        posX = 370,
        posY = 750,
        text = "Next" +
                "turn",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(10, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            rootService.playerActionService.nextTurn()
        }

    }

    init {
        addComponents(
            drawPile,
            drawPileCount,
            discardPile,
            middleCards,
            currentPlayerHand,
            currentPlayerScoringPile,
            currentPlayerName,
            currentPlayerScore,
            otherPlayerHand,
            otherPlayerScoringPile,
            otherPlayerName,
            otherPlayerScore,
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
                    height = 231,
                    width = 150,
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
        currentPlayerScore.text = "Score: " + game.players[game.currentPlayerIndex].score.toString()
        currentPlayerHand.clear()
        currentPlayerScoringPile.clear()


        //get the index of other player with modulo
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]
        otherPlayerName.text = otherPlayer.name
        otherPlayerScore.text = "Score: " + otherPlayer.score.toString()
        otherPlayerHand.clear()
        otherPlayerScoringPile.clear()

        game.players[game.currentPlayerIndex].hand.forEach { card ->
            currentPlayerHand.add(
                (cards[card] as CardView).apply {
                    applyHoverEffect(this)
                }
            )
        }
        //adjust the scoring pile of current player
        if(game.players[game.currentPlayerIndex].scoringPile.isNotEmpty()){
            game.players[game.currentPlayerIndex].scoringPile.last().forEach { card ->
                currentPlayerScoringPile.add(
                    (cards[card] as CardView).apply {
                        this.showFront()
                    }
                )
            }
        }


        //show back cards on the otherPlayer area
        otherPlayer.hand.forEach { card ->
            otherPlayerHand.add(
                (cards[card] as CardView).apply {
                    removeHoverEffect(this)
                }
            )
        }

        if(otherPlayer.scoringPile.isNotEmpty()) {
            otherPlayer.scoringPile.last().forEach { card ->
                otherPlayerScoringPile.add(
                    (cards[card] as CardView).apply {
                        this.showFront()
                    }
                )

            }
        }


    }

    override fun refreshAfterPlayCard(playedCard: Card) {
        val game = rootService.currentGame ?: return

        val playedCardView = (cards[playedCard] as CardView).apply {
            isDraggable = false
            onMouseEntered = null
            onMouseExited = null
        }
        currentPlayerHand.remove(playedCardView)
        middleCards.add(playedCardView)
    }

    override fun refreshAfterTakeTrio() {
        val game = rootService.currentGame ?: return
        middleCards.clear()
        game.players[game.currentPlayerIndex].scoringPile.last().forEach() { card ->
            currentPlayerScoringPile.add(
                (cards[card] as CardView).apply {
                    this.showFront()
                }
            )
        }
        currentPlayerScore.text = "Score: " + game.players[game.currentPlayerIndex].score.toString()
    }

    override fun refreshAfterDiscardPrompt(currentPlayer: Player) {
         Dialog(
            dialogType = DialogType.WARNING,
            title = "Discard",
            header = "test",
            message = "You have more than 8 cards, you need to discard card before ending your turn."
             )
        //TODO dialog doesnt work

    }
    override fun refreshAfterDiscardCard(discardedCard: Card) {
        val discardedCardView = cards[discardedCard] as CardView
        currentPlayerHand.remove(discardedCardView) //TODO deleting operation could be inside onDragDropped

        discardPile.add(discardedCardView.apply {
            isDraggable = false
            onMouseEntered = null
            onMouseExited = null
        })

    }

    override fun refreshAfterDrawCard(drawnCard: Card) {
        val game = rootService.currentGame ?: return
        val drawnCard = (cards[drawnCard] as CardView)

        //drawPile is just a visual placeholder, it doesn't hold any logic in the ui side
        //add the drawnCard into drawPile just to animate it correctly by selecting drawPile as the source of animation
        drawPile.add(drawnCard)

        //moving animation of the drawnCard to currentPlayerHand
        this.playAnimation(
            MovementAnimation.toComponentView(
                componentView = drawnCard,
                toComponentViewPosition = currentPlayerHand.last(),
                scene = this,
                duration = 1150
            ).apply {
                onFinished = {
                    //remove the CardView from drawPile
                    //add it to currentPlayer
                    drawnCard.removeFromParent()
                    currentPlayerHand.add(drawnCard)
                    applyHoverEffect(drawnCard)

                }
            }
        )
        drawPileCount.text = game.drawPile.size.toString()
    }
    //enable hover effect
         private fun applyHoverEffect(cardView: CardView) {
            cardView.onMouseEntered = {
                cardView.posY -= 25
            }
            cardView.onMouseExited = {
                cardView.posY += 25
            }

            cardView.width = 150.0
            cardView.height = 231.0
            cardView.rotation = 0.0
            cardView.showFront()
            cardView.isDraggable = true
        }
        //disable hover effect
         private fun removeHoverEffect(cardView: CardView) {
            cardView.onMouseEntered = null
            cardView.onMouseExited = null
            cardView.width = 150.0
            cardView.height = 231.0
            cardView.rotation = 0.0
            cardView.showBack()
            cardView.isDraggable = false
        }

}


