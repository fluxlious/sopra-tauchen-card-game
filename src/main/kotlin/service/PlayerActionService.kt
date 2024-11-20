package service

import entity.*

/**
 * This service provides functionality/logic related to player actions, such as playing a card,
 * drawing a card, swapping cards from middle and some helper functions to help these actions functionalities.
 *
 * @param rootService The root service that this service connects to
 */

class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     *  Plays the given card from the current player's hand by removing from the hand and adding to the middle
     *  if the card is valid play.A valid play means that the selected card matches the middle cards by either value or suit.
     *
     *  After a card is played, the player’s action is marked as taken, this prevents additional actions in the same turn.
     *  If the play results in three cards in the middle, the trio is collected by the player, and points are awarded.
     *  In this case, the swap ability (`hasSwapped`) is reset for both players to allow one swap in the next round.
     *
     * Additionally, if the player has just drawn a card, they are allowed to play that specific card in the same turn.
     *
     *  @param card The card to be played
     *  @throws IllegalStateException if no game is active
     *  @throws IllegalStateException if no game is currently active
     *  @throws IllegalArgumentException if the card is not valid checked by [isCardValid]
     */

    fun playCard(card: Card){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        if(!currentPlayer.hand.contains(card)){
            throw IllegalArgumentException("The card should be in the players hand")
        }

        if (!isCardValid(card)) {
            throw IllegalArgumentException("The card is not valid and cannot be played.")
        }

        // Check if the card is a normal play or the last drawn card
        if (card != currentPlayer.lastDrawnCard && currentPlayer.hasActionTaken) {
            println(currentPlayer.hand)
            throw IllegalStateException("You can only play the last drawn card in the same turn.")

        }
        middleCards.add(card)
        currentPlayer.hand.remove(card)
        currentPlayer.hasActionTaken = true
        onAllRefreshables { refreshAfterPlayCard(card) }

        // Check trio formation if there are three cards in the middle after playing the card
        if (middleCards.size == 3) {
            takeTrioAndAddToScoringPile()
            onAllRefreshables { refreshAfterTakeTrio() }
            // Reset swap ability for both players
            game.players.forEach { it.hasSwapped = false }
        }

        //nextTurn()

    }

    /**
     * a helper function for playCard that determines the type of trio (suit/value)
     * and takes the trio from middleCards and adds to current players' scoringPile as a list
     * also updates the score corresponding to the trio type that has been formed.
     *
     * @throws IllegalStateException if no game is active
     */
    private fun takeTrioAndAddToScoringPile() {
        val game = rootService.currentGame
        checkNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        //stores a boolean that indicates that they form a value trio
        // by comparing the first card of the middle with each card in the middle
        val isValueTrio = middleCards.all { it.value == middleCards.first().value }


        if(isValueTrio){//if the middle forms a value trio
            currentPlayer.score += 20

        }

        else{ // if the middle forms a suit trio
            currentPlayer.score += 5
        }
        //scoringPile stores list of cards so the trio is converted into a list.
        val trio = middleCards.toList()
        currentPlayer.scoringPile.add(trio)
        //println("${currentPlayer.name} has scored ${trio.toString()}")
        middleCards.clear()
    }

    /**
     *
     * Removes a card from the drawPile and adds it to the current players' hand
     * if after drawing the card, drawPile is empty, triggers [playLastCard]
     *
     * @throws IllegalStateException if no game is active
     * @throws IllegalStateException if the current players' hand is more than 8 cards.
     * @throws IllegalStateException if the drawPile is empty
     */
    fun drawCard(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        if(game.drawPile.isEmpty()) {
            throw IllegalStateException("Draw pile is empty")
        }
        if(currentPlayer.hasActionTaken){
            throw IllegalStateException("Action taken")
        }
        val drawnCard = game.drawPile.pop() // draw the card from drawPile

        currentPlayer.hand.add(drawnCard)
        currentPlayer.hasActionTaken = true
        currentPlayer.lastDrawnCard = drawnCard
        onAllRefreshables { refreshAfterDrawCard(drawnCard) }
        // if the drawPile is empty after the draw, we set the game over but allow playing the card
        if(game.drawPile.isEmpty()){
            playLastCard()
        }

    }

    /** Special function of the play card, allows player to play the last drawn card after drawing the last card.
     * if the last drawn card is not valid, it is checked by [isCardValid]
     * if the last drawn card is valid, plays the card on middle, checks the trio formation similar to playCard()
     * and ends the game.
     *
     * This function gets trigger only when the drawPile becomes empty as it allows the player to play their last drawn card.
     *
     * @throws IllegalStateException if no game is active
     */
    private fun playLastCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        val lastDrawnCard = currentPlayer.lastDrawnCard
        checkNotNull(lastDrawnCard) { "No last drawn card to play." }

        // Check if the last drawn card can be played
        if (!isCardValid(lastDrawnCard)) {
            // If the last drawn card cannot be played, end the game
            rootService.gameService.endGame()
            return
        }

        // Play the card to the middle if it's valid
        game.middleCards.add(lastDrawnCard)
        currentPlayer.hand.remove(lastDrawnCard)
        currentPlayer.lastDrawnCard = null
        onAllRefreshables {
            refreshAfterPlayCard(lastDrawnCard)
        }

        // Check if the placement forms a trio and take it if valid
        if (game.middleCards.size == 3) {
            takeTrioAndAddToScoringPile()


        }
        rootService.gameService.endGame()
    }
   /** Swaps a card from the current player's hand with a card from the middle if the swap is valid.
    * The swap is allowed if the player has not already swapped or taken an action in the current turn.
    *
    * After the swapping is successful, the corresponding booleans are marked. (hasActionTaken, hasSwapped)
    * and the GUI is refreshed.
    *
    * @param cardFromHand the selected card from the players' hand to be swapped with cardFromMiddle.
    * @param cardFromMiddle the selected card in the middle to be swapped with cardFromHand
    *
    * @throws IllegalStateException if no game is active
    * @throws IllegalStateException if the player has already swapped
    * @throws IllegalStateException if the there is no card in the middle
    * @throws IllegalStateException if the player has already taken an action
    * @throws IllegalArgumentException if the selected card from hand is not in the hand
    * or the selected card in the middle is not in middle
    * @throws IllegalArgumentException if the swap is not valid, checked by isSwapValid()
    */
    fun swapCard(cardFromHand: Card, cardFromMiddle: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { }

        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        if (currentPlayer.hasSwapped) {
            throw IllegalStateException("You have already swapped this round.")
        }

        if(middleCards.isEmpty()){
            throw IllegalStateException("The middle is empty")
        }

        if (currentPlayer.hasActionTaken) {
            throw IllegalStateException("You have already taken an action this turn.")
        }

        // Ensure the selected cards are in the correct locations
        if (!currentPlayer.hand.contains(cardFromHand) || !middleCards.contains(cardFromMiddle)) {
            throw IllegalArgumentException("The selected cards for swap are not in the correct locations.")
        }


        if(isSwapValid(cardFromHand, cardFromMiddle, middleCards)) {
            //keep the indexes for animation purposes
            val handCardIndex = currentPlayer.hand.indexOf(cardFromHand)
            val middleCardIndex = middleCards.indexOf(cardFromMiddle)


            currentPlayer.hand[handCardIndex] = cardFromMiddle
            middleCards[middleCardIndex] = cardFromHand

            println("Middle: " + game.middleCards)
            println("Hand of ${currentPlayer.name} ${currentPlayer.hand}")
            // Mark the swap as used and action taken
            currentPlayer.hasSwapped = true
            currentPlayer.hasActionTaken = true
            onAllRefreshables { refreshAfterSwapCard(cardFromHand, cardFromMiddle) }
            onAllRefreshables { refreshAfterSwapCard(cardFromHand, cardFromMiddle) }

        }
        else{
            throw IllegalArgumentException("The card is not valid")
        }


    }

    /**
     * a helper function of swapCard that checks
     * if swapping a card from the player's hand with a card from the middle
     * maintains valid trio potential in the middle.
     *
     * The swap is considered valid if, after performing the swap, all cards in the middle
     * either share the same suit or the same value. Not to manipulate the actual middle cards,
     * the swapping and trio check is made on the copy of the middle cards.
     *
     * @throws IllegalStateException if no game is active
     * @param cardFromHand the card from the players' hand to be swapped with cardFromMiddle.
     * @param cardFromMiddle the card in the middle to be swapped with cardFromHand
     * @param middleCards the list of current middle cards
     *
     * @return true if the temporary swap maintains potential trio by matching value or suit.
     */
    private fun isSwapValid(cardFromHand: Card, cardFromMiddle: Card, middleCards : MutableList<Card>) : Boolean {
        //We take a copy of the middleCards not to manipulate the real middleCards in case the swap action is not valid
        val temporaryMiddleCards = middleCards.toMutableList()
        temporaryMiddleCards.add(cardFromHand)
        temporaryMiddleCards.remove(cardFromMiddle)

        // Check if all cards in the simulated middle have the same suit or value
        return temporaryMiddleCards.all { it.suit == cardFromHand.suit } ||
                temporaryMiddleCards.all { it.value == cardFromHand.value }


    }

    /**
     * Removes the given card from the current players' hand and adds it to discardPile.
     * Used when the player got >8 cards in the hand.
     * refreshes the GUI after the discard action takes place in game logic
     *
     * @param card the card that the player wants to discard
     *
     * @throws IllegalStateException if no game is active
     * @throws IllegalStateException if the given card is not in current players' hand
     */
    fun discardCard(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        if(!currentPlayer.hand.contains(card)){

            throw IllegalArgumentException("The card is not in the hand")
        }

        currentPlayer.hand.remove(card)
        game.discardPile.push(card)
        onAllRefreshables { refreshAfterDiscardCard(card) }
    }

    /**
     * Checks if the given card can be played based on the current middle
     *
     * @param card The card to be checked
     * @return true if the [card] can be legally played if it matches the middle cards either by suit or value
     * @throws IllegalStateException if no game is currently active
     */
    fun isCardValid(card: Card): Boolean {
        val game = rootService.currentGame
        checkNotNull(game)
        val middleCards = game.middleCards

        //Checks if the given [card] matches all middle cards either by suit or value.

        return middleCards.all { middleCard -> //If the middle is empty and there is no cards to compare,
            middleCard.suit == card.suit || middleCard.value == card.value // all returns true, allowing any card to be played.

        }
    }

    /**
     *  Advances the game to the next turn,
     *  resetting the current player's action trackers(hasSwapped, hasActionTaken and lastDrawnCard)
     *  if currentPlayer has more than 8 cards, it prompts the player to discard a card before advancing to the next player
     *  if currentPlayer has less than 8 cards, it ends the turn by GameService.endTurn
     *
     *  @throws IllegalStateException if no game is active
     *  @throws IllegalStateException if the player didn't take any action in the current round
     */
    fun nextTurn(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        if(currentPlayer.hasActionTaken){
            if(currentPlayer.hand.size > 8){
                onAllRefreshables { refreshAfterDiscardPrompt(currentPlayer) }
                throw IllegalStateException("You need to discard a card before ending the turn")
            }
            currentPlayer.hasActionTaken = false
            currentPlayer.hasSwapped = false
            currentPlayer.lastDrawnCard = null

            rootService.gameService.endTurn()
            println(" after next Turn middle: " + game.middleCards)
        }
        else{
            throw IllegalStateException("You didnt perform any action.")
        }

    }
}