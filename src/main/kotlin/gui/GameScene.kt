package gui

import entity.Card
import entity.CardSuit
import entity.CardValue

import service.CardImageLoader

import service.RootService

import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.CardStack
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.CardView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Color


class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ImageVisual("game_background4.png")), Refreshable {

    private val cardImageLoader: CardImageLoader = CardImageLoader()
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()

    private var selectedCardFromHand: CardView? = null
    private var selectedCardFromMiddle: CardView? = null
    private val xShift = 70// variable to move each component in x

    private var drawPile = CardStack<CardView>(
        height = 231,
        width = 150,
        posX = 370- xShift,
        posY = 425 ,
        visual = cardImageLoader.backImage,
    )

    private val drawPileCount = Label(
        height = 231,
        width = 150,
        posX = 370- xShift,
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
        posX = 1420 - xShift,
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
        width = 480,
        posX = 750 - xShift,
        posY = 425,
        spacing = 30,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {

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
    private val overlayPane = Pane<ComponentView>(
        height = 251,
        width = 800,
        posX = 560- xShift,
        posY = 750,
        visual = ColorVisual(Color(0x81323B))
    ).apply {
        isVisible = false
    }


    //swap button clicked -> swap mode activated -> two cards clicked  -> handle the card selections -> perform swap action
    // -> reset the changes after both successful and unsuccessful
    private fun swapMode() {
        //in the swapping mode
        //allow the cards be clickable in the middle and currentPlayerHand \

        currentPlayerHand.forEach { cardView ->
            cardView.isDraggable = false
            cardView.onMouseClicked = {
                handleCardSelection(cardView, isFromHand = true) // when the card is click-selected
            }
        }

        middleCards.forEach { cardView ->
            cardView.onMouseClicked = {
                cardView.isDraggable = false
                handleCardSelection(cardView, isFromHand = false) // when the card is click-selected
            }
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

    private fun tryPerformSwap() {
        if (selectedCardFromHand != null && selectedCardFromMiddle != null) {
            val cardFromHand = cards.backward(selectedCardFromHand!!)
            val cardFromMiddle = cards.backward(selectedCardFromMiddle!!)

            try {
                rootService.playerActionService.swapCard(cardFromHand, cardFromMiddle)
                //println("Swap successful!")
            } catch (e: Exception) {
                println("Swap failed: ${e.message}")
            } finally {
                resetSwap()
            }
        }
    }
    //Reset the changes made in the swap mode and continue with normal card settings
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

    private var currentPlayerHand = LinearLayout<CardView>(
        height = 251,
        width = 800,
        posX = 560- xShift,
        posY = 750,
        spacing = -30,
        visual = ColorVisual(255, 255, 255, 50),
        alignment = Alignment.CENTER
    )

    private var otherPlayerHand = LinearLayout<CardView>(
        height = 251,
        width = 800,
        posX = 560- xShift,
        posY = 50,
        spacing = -30,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {
        rotation = 180.0
    }

    private var currentPlayerScoringPile = LinearLayout<CardView>(
        height = 251,
        width = 350,
        posX = 1420- xShift,
        posY = 750,
        spacing = -65,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )
    private var otherPlayerScoringPile = LinearLayout<CardView>(
        height = 251,
        width = 350,
        posX = 1420 - xShift,
        posY = 50,
        spacing = -65,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)

    )
    private var currentPlayerScore = Label(
        height = 50,
        width = 170,
        posX = 1420- xShift,
        posY = 1002,
        text = "Score: 0",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")
    )

    private var otherPlayerScore = Label(
        height = 50,
        width = 170,
        posX = 1420- xShift,
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
        posX = 560- xShift,
        posY = 1002,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")

    )

    private val otherPlayerName = Label(
        height = 50,
        width = 200,
        posX = 560- xShift,
        posY = 0,
        text = "Spieler",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(30, Color(0xFFFFFFF), "Staatliches")

    )
   //test button for testing the ResultMenuScene
    private val endGameTest = Button(
        height = 120,
        width = 150,
        posX = 150,
        posY = 0,
        text = "endgametest",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(10, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            rootService.gameService.endGame()
        }

    }
    private val nextTurnButton = Button(
        height = 120,
        width = 150,
        posX = 370- xShift,
        posY = 750,
        text = "Next turn",
        alignment = Alignment.CENTER,
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(25, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            rootService.playerActionService.nextTurn()
            drawPileCount.isDisabled= true // not to allow the player without clicking ready
            overlayPane.clear()

            overlayPane.isVisible = true
            // Add a label with the current player's name to the overlayPane
            overlayPane.add(
                Label(
                    height = 65,
                    width = 800,
                    posX = (overlayPane.width - 800) / 2,
                    posY = (overlayPane.height - 60) / 2 - 60,
                    text = "${currentPlayerName.text}'s Turn",
                    alignment = Alignment.CENTER,
                    font = Font(65, Color(0xE7EFF2), "JetBrains Mono ExtraBold")
                )
            )


            overlayPane.add(
                Button(
                    text = "Ready",
                    width = 200,
                    height = 80,
                    posX = (overlayPane.width - 200) / 2,
                    posY = (overlayPane.height - 60) / 2 + 50,  // Slightly below the label
                    alignment = Alignment.CENTER,
                    font = Font(30, Color(0xE7EFF2), "JetBrains Mono ExtraBold"),
                    visual = ColorVisual(Color(0x0C2027))
                ).apply {
                    onMouseClicked = {
                       drawPileCount.isDisabled= false
                        overlayPane.isVisible = false
                    }
                }
            )


        }

    }
    private val swapButton = Button(
        height = 132,
        width = 150,
        posX = 370- xShift,
        posY = 750 + 120 + 50,
        text = "Swap card",
        visual = ColorVisual(Color(0x0C2027)),
        font = Font(24, Color(0xFFFFFFF), "Staatliches")

    ).apply {
        onMouseClicked = {
            swapMode()
        }
    }

    init {
        addComponents(

            currentPlayerHand,
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
            drawPile,
            drawPileCount,
            endGameTest,
            overlayPane,
        )


    }
    override fun refreshAfterStartNewGame() {
        //Create the CardViews for each value and suit combination
        // and add it to BidirectionalMap
        val game = rootService.currentGame ?: return

        //Clear swapping selection
        selectedCardFromHand = null
        selectedCardFromMiddle = null
        middleCards.clear()
        discardPile.clear()
        drawPile.clear()

        //Show the number of cards left in the drawPile
        drawPileCount.text = game.drawPile.size.toString()

        //Create the CardViews for each value and suit combination
        // and map them into the corresponding [entity.Card] objects
        if(cards.isEmpty()) {// check for optimization because [cards] is reusable for restart game
            val preLoadedBackImage : Visual = cardImageLoader.backImage //Load the back image once for faster loading

            CardValue.values().forEach { value ->
                CardSuit.values().forEach { suit ->
                    cards[Card(suit, value)] = CardView(
                        height = 231,
                        width = 150,
                        front = cardImageLoader.frontImageFor(suit, value),
                        back  = preLoadedBackImage
                    )
                }
        }



        }
    }

    override fun refreshAfterStartTurn() {

        //Get the current game from the rootService
        val game = rootService.currentGame ?: return

        //Set the currentPlayer components for the new current player
        currentPlayerName.text = game.players[game.currentPlayerIndex].name
        currentPlayerHand.clear()
        currentPlayerScoringPile.clear()

        //Reset the swapping related variables
        swapButton.isDisabled = false
        selectedCardFromHand = null
        selectedCardFromMiddle = null

        //Get the index of other player with modulo
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]
        //Set the otherPlayer components for the new other player
        otherPlayerName.text = otherPlayer.name
        otherPlayerHand.clear()
        otherPlayerScoringPile.clear()

        //Update both of the scores
        updateScoreView()

        game.players[game.currentPlayerIndex].hand.forEach { card ->
            currentPlayerHand.add((cards[card] as CardView).apply {
                applyHoverEffect(this)
            })
        }
        //Adjust the scoring pile of current player
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

        //Show back cards on the otherPlayer area
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

        //Store the indexes of both CardViews to animate the swapping of the cards better and accurately
        val handCardIndex = currentPlayerHand.components.indexOf(cardFromHandView)
        val middleCardIndex = middleCards.components.indexOf(cardFromMiddleView)


        if (handCardIndex < 0 || middleCardIndex < 0 ||
            handCardIndex >= currentPlayerHand.components.size ||
            middleCardIndex >= middleCards.components.size) {
            return
        }
        currentPlayerHand.remove(cardFromHandView)
        middleCards.add(cardFromHandView.apply {
            removeHoverEffect(this, showFront = true)}, middleCardIndex)


        middleCards.remove(cardFromMiddleView)
        currentPlayerHand.add(cardFromMiddleView.apply {
                   applyHoverEffect(this) }, handCardIndex)


//        //Movement animation from hand to middle
//        val animation = (MovementAnimation.toComponentView(
//            componentView = cardFromHandView,
//            toComponentViewPosition = middleCards.components[middleCardIndex], //Select the location of cardFromMiddleView as target of the animation
//            scene = this,
//            duration = 1800
//        ).apply {
//            onFinished = {
//                //Remove the CardView from currentPlayerHand
//                currentPlayerHand.remove(cardFromHandView)
//                middleCards.add(cardFromHandView.apply {
//                    removeHoverEffect(this, showFront = true)
//                }, middleCardIndex)
//
//            }
//        })
//
//       val animation2 = (MovementAnimation.toComponentView(
//            componentView = cardFromMiddleView,
//            toComponentViewPosition = currentPlayerHand.components[handCardIndex], //Select the location of handCardIndex  as target of the animation
//            scene = this,
//            duration = 1800
//        ).apply {
//            onFinished = {
//                middleCards.remove(cardFromMiddleView)
//                currentPlayerHand.add(cardFromMiddleView.apply {
//                    applyHoverEffect(this)
//                }, handCardIndex)
//            }
//        })
//        lock()
//        this.playAnimation(ParallelAnimation(animation, animation))
//        unlock()
//

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

        //drawPile is just a visual placeholder, it doesn't hold any logic in the front-end
        //add the drawnCard into drawPile just to animate it correctly by selecting drawPile as the source of animation
        drawPile.add(drawnCard)
        //Player played card so swapping is disabled
        swapButton.isDisabled = true

        //Determine the target position of the movement animation
        //Again just for animation purposes
        val targetPosition = if (currentPlayerHand.components.isNotEmpty()) {
            currentPlayerHand.components.last()
        } else {
            //If the hand is empty, use the position where the first card would be added
            currentPlayerHand
        }

        lock()
        //Moving animation of the drawnCard to currentPlayerHand (targetPosition)
        this.playAnimation(
            MovementAnimation.toComponentView(
                componentView = drawnCard,
                toComponentViewPosition = targetPosition,// either to the last CardView object or to the first
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
    //Enable hover effect
    private fun applyHoverEffect(cardView: CardView) {
            cardView.onMouseEntered = {
                cardView.posY -= 25
            }
            cardView.onMouseExited = {
                cardView.posY += 25
            }
            cardView.rotation = 0.0
            cardView.showFront()
            cardView.isDraggable = true
        }

    //Disable hover effect with option to show the front/back side of the card
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
    private fun lockComponents(){
        this.components.forEach { component ->
            if(component !is Button)
            component.isDisabled = true
        }
    }
    private fun unlockComponents(){
        this.components.forEach { component ->
            component.isDisabled = false
        }
    }
}


