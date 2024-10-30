package service

import entity.*

class PlayerActionService(private val rootService: RootService) {
    private val game: Tauchen
        get() = rootService.GameService.game

    private val middleCards: MutableList<Card>
        get() = game.middleCards

    private val currentPlayer: Player
        get() = game.players[game.currentPlayerIndex]

    internal fun playCard(card: Card){
        if(!currentPlayer.hand.contains(card)){
            throw IllegalArgumentException("The card should be in the players hand")
        }

        // Check if the middle is playable
        if (middleCards.isNotEmpty() && middleCards.size < 3) {
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

        //Check trio formation if there are three cards in the middle after playing the card
        if(middleCards.size == 3){
            takeTrio()
        }
        //nextTurn()
    }

    private fun takeTrio() {
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

        val trio = middleCards.toList()
        currentPlayer.scoringPile.add(trio)
        println("${currentPlayer.name} has ${trio.toString()}  in the scoring pile")
        middleCards.clear()
    }

    internal fun playLastCard(card: Card){}


    internal fun drawCard(){
        // Check if the player has 8 cards or fewer in hand
        if (currentPlayer.hand.size > 8) {
            throw IllegalStateException("Cannot draw a card: Player already has more than 8 cards in hand.")
        }
        val drawnCard = game.drawPile.pop()






    }
    internal fun swapCard() {
        if(!currentPlayer.hasSwapped){

        }
        else{
            throw IllegalStateException("You already swapped this round")
        }
        currentPlayer.hasSwapped = false
    }
    internal fun discardCard(card: Card) {
        if(currentPlayer.hand.contains(card) && currentPlayer.hand.size > 8){
            currentPlayer.hand.remove(card)
            game.discardPile.push(card)
        }
        else{
            throw IllegalArgumentException("Player doesnt have the card ${card.toString()} in the hand")
        }

    }

    internal fun nextTurn(){
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % game.players.size
        println("Turn changes to ${game.players[game.currentPlayerIndex].name}")
    }
}