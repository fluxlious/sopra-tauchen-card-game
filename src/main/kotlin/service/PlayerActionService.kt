package service

import entity.*

class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {


    fun playCard(card: Card){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        if(!currentPlayer.hand.contains(card)){
            throw IllegalArgumentException("The card should be in the players hand")
        }

        if((isCardValid(card) && !currentPlayer.hasActionTaken) || (card == currentPlayer.lastDrawnCard) ){
            //If the card is valid, play it to the middle
            middleCards.add(card)
            currentPlayer.hand.remove(card)
            currentPlayer.hasActionTaken = true
            onAllRefreshables {
                refreshAfterPlayCard(card)
            }

            //Check trio formation if there are three cards in the middle after playing the card
            if(middleCards.size == 3){
                takeTrio()
                // Reset swap ability for both players
                game.players.forEach { it.hasSwapped = false }
            }
            nextTurn()

        }
        else{
            throw IllegalArgumentException("The card is not valid")
        }

    }

    private fun takeTrio() {
        val game = rootService.currentGame
        checkNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]  // Access currentPlayer within method
        val middleCards = game.middleCards                          // Access middleCards within method
        val isValueTrio = middleCards.all({ it.value == middleCards.first().value})
        //They form value trio
        if(isValueTrio){
            currentPlayer.score += 20

        }
        //They form suit trio
        else{
            currentPlayer.score += 5
        }
        addCardsToScoringPile(currentPlayer)
    }

    private fun addCardsToScoringPile(currentPlayer : Player) {
        val game = rootService.currentGame
        checkNotNull(game)
        val middleCards = game.middleCards

        val trio = middleCards.toList()
        currentPlayer.scoringPile.add(trio)
        println("${currentPlayer.name} has scored ${trio.toString()}")
        middleCards.clear()
    }

    internal fun drawCard(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        if (currentPlayer.hand.size > 8) {
            throw IllegalStateException("Player already has more than 8 cards in hand.")
        }
        else if(game.drawPile.isEmpty()) {
            throw IllegalStateException("Draw pile is empty")
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

    fun playLastCard() {
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


        // Check if the placement forms a trio and take it if valid
        if (game.middleCards.size == 3) {
            takeTrio()
            rootService.gameService.endGame()

        }
    }

    internal fun swapCard(cardFromHand: Card, cardFromMiddle: Card) {
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
            currentPlayer.hand.remove(cardFromHand)
            currentPlayer.hand.add(cardFromMiddle)
            middleCards.remove(cardFromMiddle)
            middleCards.add(cardFromHand)

            // Mark the swap as used and action taken
            currentPlayer.hasSwapped = true
            currentPlayer.hasActionTaken = true
            onAllRefreshables { refreshAfterCardSwap(cardFromHand, cardFromMiddle) }

        }
        else{
            throw IllegalArgumentException("The card is not valid")
        }


    }

    private fun isSwapValid(cardFromHand: Card, cardFromMiddle: Card, middleCards : MutableList<Card>) : Boolean {
        //We take a copy of the middleCards not to manipulate the real middleCards in case the swap action is not valid
        val temporaryMiddleCards = middleCards.toMutableList()
        temporaryMiddleCards.add(cardFromHand)
        temporaryMiddleCards.remove(cardFromMiddle)

        // Check if all cards in the simulated middle have the same suit or value
        return temporaryMiddleCards.all { it.suit == cardFromHand.suit } ||
                temporaryMiddleCards.all { it.value == cardFromHand.value }


    }

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

    fun isCardValid(card: Card): Boolean {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }
        val middleCards = game.middleCards


        return middleCards.all { middleCard ->
            middleCard.suit == card.suit || middleCard.value == card.value
        }
    }

    fun nextTurn(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        if(currentPlayer.hasActionTaken){

            currentPlayer.hasActionTaken = false
            currentPlayer.hasSwapped = false
            currentPlayer.lastDrawnCard = null

            if(currentPlayer.hand.size > 8){
                discardCard(currentPlayer.hand.last())
                //Normally discard card will come from gui, so currentPlayer.hand.last() is just a placeholder default.
                //It will be changed when we add a GUI that asks for the player to choose a card to discard before ending his/her turn
            }

            rootService.gameService.endTurn()

        }
        else{
            throw IllegalStateException("Player didnt perform any action.")
        }

    }
}