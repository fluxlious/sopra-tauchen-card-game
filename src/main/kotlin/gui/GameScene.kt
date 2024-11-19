package gui

import entity.Card
import entity.CardSuit
import entity.CardValue

import service.CardImageLoader

import service.RootService

import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.animation.ParallelAnimation
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


class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ImageVisual("game_background4.png")), Refreshable {

    private val cardImageLoader: CardImageLoader = CardImageLoader()
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()
    private var selectedCardFromHand: CardView? = null
    private var selectedCardFromMiddle: CardView? = null

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

    private val discardPile: CardStack<CardView> = CardStack<CardView>(
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

    private val middleCards = LinearLayout<CardView>(
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

    private fun swapMode() {
        currentPlayerHand.forEach { cardView ->
            cardView.isDraggable = false
            cardView.onMouseClicked = {
                println("You clicked on ${cards.backward(cardView)}")
                handleCardSelection(cardView, isFromHand = true)
            }
        }

        middleCards.forEach { cardView ->
            cardView.onMouseClicked = {
                println("You clicked on ${cards.backward(cardView)}")
                cardView.isDraggable = false
                handleCardSelection(cardView, isFromHand = false)
            }
        }

    }
    private fun tryPerformSwap() {
        if (selectedCardFromHand != null && selectedCardFromMiddle != null) {
            val cardFromHand = cards.backward(selectedCardFromHand!!)
            val cardFromMiddle = cards.backward(selectedCardFromMiddle!!)

            try {
                rootService.playerActionService.swapCard(cardFromHand, cardFromMiddle)
                println("Swap successful!")
            } catch (e: Exception) {
                println("Swap failed: ${e.message}")
            } finally {
                resetSwap()
            }
        }
    }

    private fun resetSwap() {
        selectedCardFromHand = null
        selectedCardFromMiddle = null
        currentPlayerHand.forEach { cardView ->
            cardView.apply {onMouseClicked = null}
            applyHoverEffect(cardView)
        }

        // Remove click listeners from middle cards
        middleCards.components.forEach { cardView ->
            cardView.apply { onMouseClicked = null}
            removeHoverEffect(cardView, showFront = true)
        }
    }

    private fun handleCardSelection(cardView: CardView, isFromHand: Boolean) {
        if(isFromHand){
            selectedCardFromHand = cardView
        }
        else {
            selectedCardFromMiddle = cardView
        }
        tryPerformSwap()
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

    private var otherPlayerHand = LinearLayout<CardView>(
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

    private var currentPlayerScoringPile = LinearLayout<CardView>(
        height = 251,
        width = 300,
        posX = 1420,
        posY = 750,
        spacing = -110,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )
    private var otherPlayerScoringPile = LinearLayout<CardView>(
        height = 251,
        width = 300,
        posX = 1420,
        posY = 50,
        spacing = -110,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )
    private var currentPlayerScore = Label(
        height = 50,
        width = 170,
        posX = 1420,
        posY = 1002,
        text = "Score: 0",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")
    )

    private var otherPlayerScore = Label(
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
    private val nextTurnButton = Button(
        height = 120,
        width = 150,
        posX = 370,
        posY = 750,
        text = "Next turn",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(25, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            rootService.playerActionService.nextTurn()
        }

    }
    private val swapButton = Button(
        height = 132,
        width = 150,
        posX = 370,
        posY = 750 + 120 + 50,
        text = "Swap card",
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(22, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            swapMode()
        }
    }
//    private val overlayPane = Pane<ComponentView>(
//        posX = 0,
//        posY = 0,
//        width = 1920,
//        height = 1080,
//        visual = ColorVisual(Color(12, 32, 39))
//    ).apply {
//        this.isVisible = false
//    }
//    private val button = Button(
//        height = 132,
//        width = 150,
//        posX = 0,
//        posY = 0,
//        text = "asdasdasd",
//        visual = ColorVisual(Color(0x0C2027)),
//        font = Font(22, Color(0xFFFFFFF), "Staatliches")
//    ).apply {
//        onMouseClicked = {
//            this@GameScene.overlayPane.isVisible = true
//        }
//    }


    init {
        addComponents(

            currentPlayerHand,
            drawPile,
            drawPileCount,
            discardPile,
            currentPlayerScoringPile,
            currentPlayerName,
            currentPlayerScore,
            otherPlayerHand,
            otherPlayerScoringPile,
            otherPlayerName,
            otherPlayerScore,
            nextTurnButton,
            swapButton,
            middleCards,
//            button,
//            overlayPane
        )


    }
    override fun refreshAfterStartNewGame() {
        //create the CardViews for each value and suit combination
        // and add it to BidirectionalMap
        val game = rootService.currentGame ?: return
        //Clear swapping selection
        selectedCardFromHand = null
        selectedCardFromMiddle = null

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

        //get the current game from the rootService
        val game = rootService.currentGame ?: return

        //set the currentPlayer components for the new current player
        currentPlayerName.text = game.players[game.currentPlayerIndex].name
        currentPlayerHand.clear()
        currentPlayerScoringPile.clear()

        swapButton.isDisabled = false


        //get the index of other player with modulo
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]
        //set the otherPlayer components for the new other player
        otherPlayerName.text = otherPlayer.name
        otherPlayerHand.clear()
        otherPlayerScoringPile.clear()

        //update both of the scores
        updateScoreView()

        game.players[game.currentPlayerIndex].hand.forEach { card ->
            currentPlayerHand.add((cards[card] as CardView).apply {
                applyHoverEffect(this)
            })
        }
        //adjust the scoring pile of current player
        if(game.players[game.currentPlayerIndex].scoringPile.isNotEmpty()){
            game.players[game.currentPlayerIndex].scoringPile.forEach { trio ->
                trio.forEach { card ->
                    currentPlayerScoringPile.add(
                        (cards[card] as CardView).apply {
                            this.showFront()
                        }
                    )
                }
            }
        }

        //show back cards on the otherPlayer area
        otherPlayer.hand.forEach { card ->
            otherPlayerHand.add(
                (cards[card] as CardView).apply {
                    removeHoverEffect(this, showFront = false)
                }
            )
        }

        if(otherPlayer.scoringPile.isNotEmpty()){
            otherPlayer.scoringPile.forEach { trio ->
                trio.forEach { card ->
                    otherPlayerScoringPile.add(
                        (cards[card] as CardView).apply {
                            this.showFront()
                        }
                    )
                }
            }
        }


    }

    override fun refreshAfterPlayCard(playedCard: Card) {
        swapButton.isDisabled = false
        val playedCardView = (cards[playedCard] as CardView).apply {
            removeHoverEffect(this, showFront = true)
        }
        currentPlayerHand.remove(playedCardView)
        middleCards.add(playedCardView)
    }

    override fun refreshAfterTakeTrio() {
        val game = rootService.currentGame ?: return

        game.players[game.currentPlayerIndex].scoringPile.last().forEach { card ->

            lock()
            val middleCardView = (cards[card] as CardView)
            this.playAnimation(MovementAnimation.toComponentView(
                componentView = middleCardView,
                toComponentViewPosition = currentPlayerScoringPile,
                scene = this,
                duration = 1500
            ).apply {
                onFinished = {
                    middleCardView.removeFromParent()
                    currentPlayerScoringPile.add(middleCardView.apply {
                        this.showFront()
                    })
                    unlock()
                }
            })



        }
       updateScoreView()
    }
    override fun refreshAfterSwapCard(cardFromHand: Card, cardFromMiddle: Card) {
        val cardFromHandView = cards[cardFromHand] as CardView
        val cardFromMiddleView = cards[cardFromMiddle] as CardView
        swapButton.isDisabled = true

        val handCardIndex = currentPlayerHand.components.indexOf(cardFromHandView)
        val middleCardIndex = middleCards.components.indexOf(cardFromMiddleView)


        if (handCardIndex < 0 || middleCardIndex < 0 ||
            handCardIndex >= currentPlayerHand.components.size ||
            middleCardIndex >= middleCards.components.size) {
            return
        }
        //movement animation from hand to middle
        val animation = (MovementAnimation.toComponentView(
            componentView = cardFromHandView,
            toComponentViewPosition = middleCards.components[middleCardIndex],
            scene = this,
            duration = 2000
        ).apply {
            onFinished = {
                //remove the CardView from currentPlayerHand
                currentPlayerHand.remove(cardFromHandView)
                middleCards.add(cardFromHandView.apply {
                    removeHoverEffect(this, showFront = true)
                }, middleCardIndex)
            }
        })

       val animation2 = (MovementAnimation.toComponentView(
            componentView = cardFromMiddleView,
            toComponentViewPosition = currentPlayerHand.components[handCardIndex],
            scene = this,
            duration = 2000
        ).apply {
            onFinished = {

                middleCards.remove(cardFromMiddleView)
                currentPlayerHand.add(cardFromMiddleView.apply {
                    applyHoverEffect(this)
                }, handCardIndex)
            }
        })

        lock()
        this.playAnimation(ParallelAnimation(animation, animation2))
        unlock()
    }
    override fun refreshAfterDiscardCard(discardedCard: Card) {
        val discardedCardView = cards[discardedCard] as CardView
        currentPlayerHand.remove(discardedCardView) //TODO deleting operation could be inside onDragDropped

        discardPile.add(discardedCardView.apply {
            removeHoverEffect(this, showFront = true)
        })

    }
    override fun refreshAfterDrawCard(drawnCard: Card) {
        val game = rootService.currentGame ?: return
        val drawnCard = (cards[drawnCard] as CardView)

        //drawPile is just a visual placeholder, it doesn't hold any logic in the ui side
        //add the drawnCard into drawPile just to animate it correctly by selecting drawPile as the source of animation
        drawPile.add(drawnCard)
        //the player played card so swapping is disabled
        swapButton.isDisabled = true

        //determine the target of movement animation
        //if the hand is empty it goes to the hand
        //else it goes to the last cardView of the hand
        val targetPosition = if (currentPlayerHand.components.isNotEmpty()) {
            currentPlayerHand.components.last()
        } else {
            // If the hand is empty, use the position where the first card would be added
            currentPlayerHand
        }

        lock()
        //moving animation of the drawnCard to currentPlayerHand
        this.playAnimation(
            MovementAnimation.toComponentView(
                componentView = drawnCard,
                toComponentViewPosition = targetPosition,
                scene = this,
                duration = 1150
            ).apply {
                onFinished = {
                    //remove the CardView from drawPile
                    //add it to currentPlayer
                    drawnCard.removeFromParent()
                    currentPlayerHand.add(drawnCard)
                    applyHoverEffect(drawnCard)
                    unlock()

                }
            }
        )
        drawPileCount.text = game.drawPile.size.toString()
    }

    //Help methods:
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

    //disable hover effect with option to show the front/back side of the card
    private fun removeHoverEffect(cardView: CardView, showFront : Boolean ) {
            cardView.onMouseEntered = null
            cardView.onMouseExited = null
            cardView.width = 150.0
            cardView.height = 231.0
            cardView.rotation = 0.0
            //Show the wanted side
            if (showFront) {
                cardView.showFront()
            } else {
                cardView.showBack()
            }
            cardView.isDraggable = false
        }

    //updates the score labels for both player
    private fun updateScoreView() {
        val game = rootService.currentGame ?: return
        val currentPlayer = game.players[game.currentPlayerIndex]
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]

        currentPlayerScore.text = "Score: ${currentPlayer.score}"
        otherPlayerScore.text = "Score: ${otherPlayer.score}"

    }
}


