package service

import entity.*
import java.util.Stack
import kotlin.random.Random



//TODO KDoc


class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    //initialize the game object with an empty list of players
    fun startNewGame(playerNames: List<String>) {
        //initialize the game object with an empty list of players
        if (playerNames.size != 2) {
            throw IllegalArgumentException("Player list must have 2 players")
        }

        //Check if the players
        if(playerNames[0]==playerNames[1]) {
            throw IllegalArgumentException("Player names cannot be same")
        }

        //checks if the player names are empty
        if(playerNames[0].isEmpty() || playerNames[1].isEmpty()) {
            throw IllegalArgumentException("Player names cannot be empty")
        }

        //Initialise the Player objects and populate the list
        val players = listOf(Player(playerNames[0]), Player(playerNames[1]))

        //Initialise the game with initialised players
        val game: Tauchen = Tauchen(0, players)

        //Shuffle the standardDeck and put the shuffled cards into drawPile
        game.drawPile.addAll(createStandardDeck().shuffled())

        //Deal 5 cards to both players and removing the cards from drawPile
        for(player in players){
            repeat(5){
                player.hand.add(game.drawPile.pop())
            }
        }
        game.currentPlayerIndex= Random.nextInt(players.size)

        rootService.currentGame = game
        onAllRefreshables { refreshAfterGameStart() }
        startTurn()
    }

    fun startTurn() {
        val game = rootService.currentGame
        checkNotNull(game)
        println("${game.players[game.currentPlayerIndex].name} starts")

        onAllRefreshables {refreshAfterGameStart() }
    }

    fun endTurn() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        println("${currentPlayer.name} ends the turn")

        //change player
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % 2

        //TODO isVisible could be implemented here, for cards to be seen for each

        onAllRefreshables { refreshAfterTurnEnds() }
        startTurn()
    }

    //Creates a not-shuffled standard deck (52 cards) by creating all values for each suit (clubs, spades, hearts,diamonds)
    private fun createStandardDeck(): Stack<Card> {
        val standardDeck = Stack<Card>()
        for (suit in CardSuit.values()) {
            for (value in CardValue.values()) {
                val card = Card(suit, value)
                standardDeck.push(card)
            }
        }
        return standardDeck
    }

}

