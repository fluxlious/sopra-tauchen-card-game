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
        val isSuitTrio = middleCards.all({ it.suit == middleCards.first().suit  })
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
        // if the drawPile is empty after the draw, we set the game over but allow playing the
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

    internal fun swapCard(toSwap: Card, middleCard: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { }

        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        // Ensure the player hasn't swapped this round
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
        if (!currentPlayer.hand.contains(toSwap) || !middleCards.contains(middleCard)) {
            throw IllegalArgumentException("The selected cards for swap are not in the correct locations.")
        }


        currentPlayer.hand.remove(toSwap)
        currentPlayer.hand.add(middleCard)
        middleCards.remove(middleCard)
        middleCards.add(toSwap)

        // Mark the swap as used
        currentPlayer.hasSwapped = true
        currentPlayer.hasActionTaken = true
    }
    fun discardCard(card: Card) {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        if(currentPlayer.hand.contains(card) && currentPlayer.hand.size > 8){
            currentPlayer.hand.remove(card)
            game.discardPile.push(card)
            onAllRefreshables { refreshAfterDiscardCard(card) }
        }
        else{
            throw IllegalArgumentException("You cant discard card")
        }

    }
    private fun isCardValid(card: Card): Boolean {
        val game = rootService.currentGame
        checkNotNull(game) { "No game is currently active." }
        val middleCards = game.middleCards

        // if the middle is empty, every card is valid
        if (middleCards.isEmpty()) return true

        // Check if the card matches any middle card by suit or value
        return middleCards.any { middleCard ->
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
            rootService.gameService.endTurn()

        }
        else{
            throw IllegalStateException("Player didnt perform any action.")
        }

    }
}