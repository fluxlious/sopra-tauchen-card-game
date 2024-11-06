package service

import entity.*

class PlayerActionService(private val rootService: RootService) {

    internal fun playCard(card: Card){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        if(!currentPlayer.hand.contains(card)){
            throw IllegalArgumentException("The card should be in the players hand")
        }

        // Check if the middle is playable
        if (middleCards.isNotEmpty() && middleCards.size < 3) {
            //check if the card is valid play
            val isValidPlay = middleCards.any { middleCard ->
                middleCard.suit == card.suit || middleCard.value == card.value
            }

            if (!isValidPlay) {
                throw IllegalArgumentException("Invalid play: Card must match either suit or value of a middle card")
            }
        }

        //If the card is valid, play it to the middle
        middleCards.add(card)
        currentPlayer.hand.remove(card)
        game.players[game.currentPlayerIndex].hasActionTaken = true

        //Check trio formation if there are three cards in the middle after playing the card
        if(middleCards.size == 3){
            takeTrio()
        }
        //nextTurn()
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
        println("${currentPlayer.name} has ${trio.toString()}  in the scoring pile")
        middleCards.clear()
    }



    internal fun drawCard(){
        val game = rootService.currentGame
        // Check if the player has 8 cards or fewer in hand
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        if (currentPlayer.hand.size > 8) {
            throw IllegalStateException("Cannot draw a card: Player already has more than 8 cards in hand.")
        }
        val drawnCard = game.drawPile.pop()


    }
    internal fun swapCard(toSwap: Card, middleCard: Card) {
        val game = rootService.currentGame
        checkNotNull(game) { "Game must be initialized to perform a swap." }

        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards

        // Ensure the player hasn't swapped this round
        if (currentPlayer.hasSwapped) {
            throw IllegalStateException("You have already swapped this round.")
        }

        // Ensure the selected cards are in the correct locations
        if (!currentPlayer.hand.contains(toSwap) || !middleCards.contains(middleCard)) {
            throw IllegalArgumentException("The selected cards for swap are not in the correct locations.")
        }

        // If there are 3 cards in the middle, check if the swap will form a valid trio
        if (middleCards.size == 3) {
            val temporaryMiddleCards = middleCards.toMutableList()
            temporaryMiddleCards.remove(middleCard)
            temporaryMiddleCards.add(toSwap)

            // Check if the temporary setup forms a valid trio
            if (!isValidTrio(temporaryMiddleCards)) {
                throw IllegalArgumentException("Swap invalid: resulting middle does not form a valid trio.")
            }

            // If the trio is valid, perform the actual swap
            println("Valid trio formed after swap. Trio taken.")
            takeTrio()
        } else {
            // If fewer than 3 cards in the middle, allow the swap without trio validation
            println("Middle does not have a trio; swap allowed without validation.")
        }

        // Perform the actual swap
        currentPlayer.hand.remove(toSwap)
        currentPlayer.hand.add(middleCard)
        middleCards.remove(middleCard)
        middleCards.add(toSwap)

        // Mark the swap as used
        currentPlayer.hasSwapped = true
    }
    private fun discardCard(card: Card) {
        val game = rootService.currentGame

        if(currentPlayer.hand.contains(card) && currentPlayer.hand.size > 8){
            currentPlayer.hand.remove(card)
            game.discardPile.push(card)
        }
        else{
            throw IllegalArgumentException("Player doesnt have the card ${card.toString()} in the hand")
        }

    }
    internal boolean
}