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

/**
 * The GameScene class is a BoardGameScene that displays the game components and manages them according to the game logic.
 *
 * @param rootService The associated [RootService]
 */
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080, background = ImageVisual("game_background4.png")), Refreshable {

    private val cardImageLoader: CardImageLoader = CardImageLoader()

    // Pairs the CardView and the Card object and allows efficient access in both directions.
    // Card -> CardView and CardView -> Card
    val cards: BidirectionalMap<Card, CardView> = BidirectionalMap()

    //Used for card selection of swapping
    private var selectedCardFromHand: CardView? = null
    private var selectedCardFromMiddle: CardView? = null

    private val xShift = 70// variable to move each component in x


    //This card stack is just a visual placeholder, just stores the last drawn card to animate.
    private var drawPile = CardStack<CardView>(
        height = 231,
        width = 150,
        posX = 370- xShift,
        posY = 425 ,
        visual = cardImageLoader.backImage,
    )

    //This label displays the number of card left in the discard pile.
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
            try {
                rootService.playerActionService.drawCard()
            }
            catch (e: Exception) {
                errorPrompt.add(messageLabel)
                errorPrompt.add(okButton)

                showPromptMessage(e.message.toString())//Set the error message to the label in the error pane
            }
        }
    }

    //This card stack accepts drag and drop when the discarding is needed.
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
            //accept the dropping a card if the player has more than 8 cards in the hand
            if (game.players[game.currentPlayerIndex].hand.size > 8) {
                true
            } else {
                errorPrompt.add(messageLabel)
                errorPrompt.add(okButton)
                showPromptMessage("You cant discard a card")
                false
            }
        }
        //if it is valid dragging then trigger discarding action.
        onDragDropped = {dragEvent ->
            val cardView = dragEvent.draggedComponent as CardView
            val card = cards.backward(cardView)
            rootService.playerActionService.discardCard(card)
        }

    }

    //This linear layout holds the middle cards' CardView objects.
    private val middleCards = LinearLayout<CardView>(
        height = 251,
        width = 500,
        posX = 720,
        posY = 425,
        spacing = 30,
        alignment = Alignment.CENTER,
        visual = ColorVisual(255, 255, 255, 50)
    ).apply {

        //Accept the dropping a card when the card is a valid play
        //Also accept if the player has taken action but tries to drag the last drawn card.
        dropAcceptor = { dragEvent ->
            val game = rootService.currentGame
            checkNotNull(game)
            val draggedComponent = dragEvent.draggedComponent

            val currentPlayer = game.players[game.currentPlayerIndex]


            // Ensure the dragged component is a CardView
            if (draggedComponent is CardView) {
                val card = cards.backward(draggedComponent)

                rootService.playerActionService.isCardValid(card) &&
                        (card == currentPlayer.lastDrawnCard || !currentPlayer.hasActionTaken) //Exception to play last drawn card
            } else {
                false
            }
        }
        //Trigger the play card action in the service
        onDragDropped = { dragEvent ->
            val game = rootService.currentGame
            checkNotNull(game)

            val cardView = dragEvent.draggedComponent as CardView
            val card = cards.backward(cardView)
            if(game.drawPile.isEmpty()){
                rootService.playerActionService.playCard(card)
                rootService.gameService.endGame()
            }
            // Play the card
            rootService.playerActionService.playCard(card)

        }

    }
    //This overlayPane is used to overlay the current players' cards when the turn changes.
    private val overlayPane = Pane<ComponentView>(
        height = 251,
        width = 800,
        posX = 560- xShift,
        posY = 750,
        visual = ColorVisual(Color(0x81323B))
    ).apply {
        isVisible = false
    }

    /**
     * This help-method makes the middle's and the hand's cardViews clickable for the swap card selection.
     * and triggers the function that handles the selected cards.
     */
    private fun swapMode() {
        //in the swapping mode
        //allow the cards be clickable in the middle and currentPlayerHand
        currentPlayerHand.forEach { cardView ->
            cardView.isDraggable = false
            cardView.onMouseClicked = {
                handleCardSelection(cardView, isFromHand = true) // triggers when the card is click-selected
            }
        }
        middleCards.forEach { cardView ->
            cardView.onMouseClicked = {
                cardView.isDraggable = false
                handleCardSelection(cardView, isFromHand = false) // triggers when the card is click-selected
            }
        }
    }

    /**
     *  Marks the clicked CardView as selected for a potential card swap.
     *
     *  @param cardView The CardView that was clicked and is being selected.
     *  @param isFromHand A boolean indicating whether the selected card is from the player's hand (true)
     */
    private fun handleCardSelection(cardView: CardView, isFromHand: Boolean) {
        if(isFromHand){
            selectedCardFromHand = cardView
        }
        else {
            selectedCardFromMiddle = cardView
        }
        tryPerformSwap()
    }

    /**
     * This help-method triggers swapCard method by passing the Card objects of the selected CardViews
     *
     */
    private fun tryPerformSwap() {
        //both cards should be selected therefore not null.
        if (selectedCardFromHand != null && selectedCardFromMiddle != null) {
            val cardFromHand = cards.backward(selectedCardFromHand!!)
            val cardFromMiddle = cards.backward(selectedCardFromMiddle!!)

            try {
                rootService.playerActionService.swapCard(cardFromHand, cardFromMiddle)
            }
            catch (e: Exception) {
                errorPrompt.add(messageLabel)
                errorPrompt.add(okButton)
                showPromptMessage(e.message.toString())
            }
            finally {
                resetSwap() //Reset the changes made to the CardViews in both fail and success cases
            }
        }
    }

    /**
     * This help-method resets the changes made to the CardViews in the swap mode to ensure continuing with normal card settings.
     */
    private fun resetSwap() {
        selectedCardFromHand = null
        selectedCardFromMiddle = null

        // Remove click listeners from hand CardViews and apply hover effect to be sure.
        currentPlayerHand.forEach { cardView ->
            cardView.apply {onMouseClicked = null}
            applyHoverEffect(cardView)
        }
        // Remove click listeners from middle CardViews and remove hover effect to be sure.
        middleCards.forEach { cardView ->
            cardView.apply { onMouseClicked = null}
            removeHoverEffect(cardView, showFront = true)
        }
    }

    //The linearlayouts for the current player's hand and the other player's hand
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

    //The linearlayouts for the current player's scoring pile and the other player's scoring pile
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

    //Score labels for both players
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

    //Name label for both players
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

    private fun overlayHandCards(){
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
                font = Font(65, Color(0xE7EFF2), "Staatliches")

            )
        )

        // Add a button to allow player to play once ready.
        overlayPane.add(
            Button(
                text = "Ready",
                width = 200,
                height = 80,
                posX = (overlayPane.width - 200) / 2,
                posY = (overlayPane.height - 60) / 2 + 50,  // Slightly below the label
                alignment = Alignment.CENTER,
                font = Font(30, Color(0xE7EFF2), "Staatliches"),
                visual = ColorVisual(Color(0x0C2027))
            ).apply {
                onMouseClicked = {
                    drawPileCount.isDisabled= false
                    overlayPane.isVisible = false
                }
            }
        )
    }
    //This button is used to advance into the turn of the next player
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
            try {
                rootService.playerActionService.nextTurn()
            }
            catch(e : Exception){
                errorPrompt.add(messageLabel)
                errorPrompt.add(okButton)
                showPromptMessage(e.message.toString())//Set the error message to the label in the error pane
            }

        }
    }

    private var errorPrompt = Pane<ComponentView>(
        height = 200, // Height of the popup
        width = 400,  // Width of the popup
        posX = (1920 / 2) - (400 / 2), // Center the popup horizontally on screen
        posY = (1080 / 2) - (200 / 2), // Center the popup vertically on screen
        visual = ColorVisual(Color(12, 32, 39, 230)) // Background color
    ).apply {
        isVisible = false // Initially hidden
    }

    // Initialize the messageLabel
    private val messageLabel = Label(
        height = 30,
        width = 400,
        posX = (400 / 2) - (400 / 2), // Center horizontally relative to the parent container
        posY = 70, // Position the label
        text = "",
        font = Font(30, Color(255,255,255), "Staatliches")
    )

    // Add the OK button
    private val okButton = Button(
        height = 50,
        width = 100,
        posX = (400 / 2) - 50, // Center horizontally relative to the parent container
        posY = 120, // Position below the label
        text = "OK",
        visual = ColorVisual(Color(129, 50, 59, 230)), // Background color
        font = Font(30, Color(255,255,255), "Staatliches")
    ).apply {
        onMouseClicked = {

            errorPrompt.isVisible = false // Hide the prompt when the button is clicked
            errorPrompt.clear()
        }
    }
    // This button is used to get into the swapping mode.
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
    //Initialise the scene with added components
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
            errorPrompt,
            overlayPane
        )


    }
    /**
     * This method is called by the [service.GameService] when the game has started.
     */
    override fun refreshAfterStartNewGame() {
        //Create the CardViews for each value and suit combination
        // and add it to BidirectionalMap
        val game = rootService.currentGame ?: return

        //Clear swapping selections
        selectedCardFromHand = null
        selectedCardFromMiddle = null

        //Refresh the game components for the new game.
        middleCards.clear()
        discardPile.clear()
        drawPile.clear()
        //Show the number of cards in the drawPile
        drawPileCount.text = game.drawPile.size.toString()
        //Create the CardViews for each value and suit combination
        //and map them into the corresponding [entity.Card] objects
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
    /**
     * This method is called by the [service.GameService], before each turn starts.
     */
    override fun refreshAfterStartTurn() {

        //Get the current game from the rootService
        val game = rootService.currentGame ?: return

        drawPileCount.isDisabled = true // gets enabled once the player clicks on the ready button
        //Set the currentPlayer components for the new current player
        currentPlayerHand.clear()
        currentPlayerScoringPile.clear()

        //Reset the swapping related variables
        swapButton.isDisabled = false
        selectedCardFromHand = null
        selectedCardFromMiddle = null

        overlayHandCards()

        //Get the index of other player with modulo
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]

        //Set the otherPlayer components for the new other player
        otherPlayerName.text = otherPlayer.name
        otherPlayerHand.clear()
        otherPlayerScoringPile.clear()

        //Update both of the scores
        updateScoreAndNameView()

        //if middle empty swap is not allowed
        if(game.middleCards.isEmpty()){
            swapButton.isDisabled = true
        }
        //Show the cards in the hand with corresponding CardView objects
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

        //Show back cards on the otherPlayerHand
        otherPlayer.hand.forEach { card ->
            otherPlayerHand.add(
                (cards[card] as CardView).apply {
                    removeHoverEffect(this, showFront = false) //shows the back side
                }
            )
        }

        //Adjust the scoring pile of current player
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

    /**
    * This method is called by the [service.GameService] when a card has been played.
     *
     * @param playedCard the Card object has been played into the middle.
    */
    override fun refreshAfterPlayCard(playedCard: Card) {
        swapButton.isDisabled = false
        val playedCardView = (cards[playedCard] as CardView).apply {
            removeHoverEffect(this, showFront = true)
        }
        currentPlayerHand.remove(playedCardView)
        middleCards.add(playedCardView)
    }

    /**
     * This method is called by the [service.GameService] when a trio has been made and taken into scoring pile.
     */
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
       updateScoreAndNameView()
    }

    /**
     * This method is called by the [service.GameService] when a card from middle and a card from the hand has been swapped.
     *
     *  @param cardFromHand the Card object that has been swapped from the middle cards.
     *  @param cardFromMiddle the Card object that has been swapped from hand.
     */
    override fun refreshAfterSwapCard(cardFromHand: Card, cardFromMiddle: Card) {
        val cardFromHandView = cards[cardFromHand] as CardView
        val cardFromMiddleView = cards[cardFromMiddle] as CardView
        swapButton.isDisabled = true

        //Store the indexes of both CardViews to animate the swapping of the cards better and accurately
        val handCardIndex = currentPlayerHand.components.indexOf(cardFromHandView)
        val middleCardIndex = middleCards.components.indexOf(cardFromMiddleView)


        if (handCardIndex < 0 || middleCardIndex < 0 ||
            handCardIndex >= currentPlayerHand.components.size ||
            middleCardIndex >= middleCards.components.size
        ) {
            return
        }
        currentPlayerHand.remove(cardFromHandView)
        middleCards.add(cardFromHandView.apply {
            removeHoverEffect(this, showFront = true)
        }, middleCardIndex)


        middleCards.remove(cardFromMiddleView)
        currentPlayerHand.add(cardFromMiddleView.apply {
            applyHoverEffect(this)
        }, handCardIndex)


    }
    /**
     * This method is called by the [service.GameService] when a card has been discarded into discardPile
     *
     *  @param discardedCard the Card object that has been discarded.
     */
    override fun refreshAfterDiscardCard(discardedCard: Card) {
        val discardedCardView = cards[discardedCard] as CardView
        currentPlayerHand.remove(discardedCardView) //TODO deleting operation could be inside onDragDropped

        discardPile.add(discardedCardView.apply {
            removeHoverEffect(this, showFront = true)
        })

    }

    /**
     * This method is called by the [service.GameService] when a card has been drawn.
     *
     *  @param drawnCard the Card object that has been drawn.
     */
    override fun refreshAfterDrawCard(drawnCard: Card) {
        val game = rootService.currentGame ?: return
        val drawnCardView = (cards[drawnCard] as CardView)

        //Add the drawnCard into drawPile just to animate it correctly by selecting drawPile as the source of animation
        drawPile.add(drawnCardView)

        //Player has taken action so swapping is disabled
        swapButton.isDisabled = true

        // Determine the target position of the movement animation
        // Again just for animation purposes
        val targetPosition = if (currentPlayerHand.components.isNotEmpty()) {
            currentPlayerHand.components.last()
        } else {
            // If the hand is empty, use the position where the first card would be added
            currentPlayerHand
        }

        lock()
        // Moving animation of the drawnCard to currentPlayerHand (targetPosition)
        this.playAnimation(
            MovementAnimation.toComponentView(
                componentView = drawnCardView,
                toComponentViewPosition = targetPosition,// either to the last CardView object or to the first
                scene = this,
                duration = 1150
            ).apply {
                onFinished = {
                    //remove the CardView from drawPile
                    //add it to currentPlayer
                    drawnCardView.removeFromParent()
                    currentPlayerHand.add(drawnCardView)
                    applyHoverEffect(drawnCardView)
                    unlock()

                }
            }
        )
        drawPileCount.text = game.drawPile.size.toString()
    }

    /**
     * This help-method disables hover effect and draggability of the CardView.
     * shows the front side of the cardView
     *
     *  @param cardView the CardView for which the effects will be applied.
     */
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

    /**
     *  This help-method disables hover effect and draggability of the CardView with the option to show the front/back side of the card
     *
     *  @param cardView the CardView for which the hovering effect will be removed.
     */
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

    /**
     * This help-method updates the score labels and name labels for both player
     */
    private fun updateScoreAndNameView() {
        val game = rootService.currentGame ?: return

        val currentPlayer = game.players[game.currentPlayerIndex]
        val otherPlayer = game.players[(game.currentPlayerIndex + 1) % 2]

        currentPlayerName.text = currentPlayer.name
        otherPlayerName.text = otherPlayer.name

        currentPlayerScore.text = "Score: ${currentPlayer.score}"
        otherPlayerScore.text = "Score: ${otherPlayer.score}"

    }
    private fun showPromptMessage(message : String) {
        val messageLabel = errorPrompt.find { it is Label } as Label // find the label in the pane
        messageLabel.text = message //set the new message
        errorPrompt.isVisible = true

    }

}


