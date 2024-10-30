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
        val currentPlayer = game.players[game.currentPlayerIndex]  // Access currentPlayer within method
        val middleCards = game.middleCards                          // Access middleCards within method


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
        nextTurn()
    }

    private fun takeTrio() {
        val currentPlayer = game.players[game.currentPlayerIndex]  // Access currentPlayer within method
        val middleCards = game.middleCards                          // Access middleCards within method
        val isSuitTrio = middleCards.all({ it.suit == middleCards.first().suit  })
        val isValueTrio = middleCards.all({ it.value == middleCards.first().value})
        //They form value trio
        if(isValueTrio){
            currentPlayer.score =+ 20

        }
        //They form suit trio
        else{
            currentPlayer.score += 5
        }
        addCardsToScoringPile(currentPlayer)
    }
    private fun addCardsToScoringPile(currentPlayer : Player) {
        val currentPlayer = game.players[game.currentPlayerIndex]  // Access currentPlayer within method
        val middleCards = game.middleCards // Access middleCards within method

        //move middle
        currentPlayer.scoringPile.add(middleCards.toList())
        middleCards.clear()
    }

    internal fun playLastCard(card: Card){}


    internal fun drawCard(){
        val currentPlayer = game.players[game.currentPlayerIndex]
        val middleCards = game.middleCards
        val drawPile = game.drawPile
        if(!currentPlayer.hasSwapped){
        //TODO drawCard logikini bitir bunu yaparken playLastCard i da implemente etmek lazim

        }
        else{
            throw IllegalStateException("You already swapped this round")
        }
        currentPlayer.hasSwapped = false


    }
    internal fun swapCard() {}
    internal fun discardCard(card: Card) {
        val currentPlayer = game.players[game.currentPlayerIndex]
        if(currentPlayer.hand.contains(card)){
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