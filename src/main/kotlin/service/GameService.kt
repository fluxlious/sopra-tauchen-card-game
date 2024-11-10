package service

import entity.*
import java.util.Stack
import kotlin.random.Random

/**
 * Service layer class which manages the game logic that includes [startNewGame], [endTurn], [endGame], [createStandardDeck]
 *
 * @param rootService The root service, which provides access to all other services
 */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {
    /**
     * Starts a new game with the given players.
     *
     * @param playerNames the String list of two players
     */
    fun startNewGame(playerNames: List<String>) {

        if (playerNames.size != 2) {
            throw IllegalArgumentException("Player list must have 2 players")
        }

        //Check if the players
        if(playerNames[0]==playerNames[1]) {
            throw IllegalArgumentException("Player names cannot be same")
        }

        //Checks if the player names are empty
        if(playerNames[0].isEmpty() || playerNames[1].isEmpty()) {
            throw IllegalArgumentException("Player names cannot be empty")
        }
        //Initialise the two players with the names from the list
        val players = listOf(Player(playerNames[0]), Player(playerNames[1]))
        //Initialise the game with initialised players
        val game = Tauchen(0, players)

        //Shuffle the standardDeck and put the shuffled cards into drawPile
        game.drawPile.addAll(createStandardDeck().shuffled())

        //Deal 5 cards to both players and remove the cards from drawPile
        for(player in players){
            repeat(5){
                player.hand.add(game.drawPile.pop())
            }
        }

        //Start from random index between 0 and 1
        game.currentPlayerIndex= Random.nextInt(players.size)
        rootService.currentGame = game

        //Trigger refresh to tell the GUI that a new game has started
        onAllRefreshables { refreshAfterStartNewGame() }

        //The first round starts automatically after the game starts
        startTurn()
    }
    /**
     * Starts the turn of the current player
     */
    fun startTurn() {
        val game = rootService.currentGame
        checkNotNull(game)

        //println("${game.players[game.currentPlayerIndex].name} starts")

        //Trigger refresh to tell the GUI that the turn has started
        onAllRefreshables {refreshAfterStartTurn() }

    }
    /**
     * Ends the turn of the current player, changes the currentPlayerIndex and starts the turn of the next player
     */
    fun endTurn() {
        val game = rootService.currentGame
        checkNotNull(game)

        //println("${currentPlayer.name} ends the turn")

        //Points to the next player
        game.currentPlayerIndex = (game.currentPlayerIndex + 1) % 2

        //Trigger refresh to tell the GUI that the turn has ended
        onAllRefreshables { refreshAfterTurnEnd() }

        //Starts the turn of the next player
        startTurn()
    }
    /**
     * Ends the game
     */
    fun endGame(){
        val game = rootService.currentGame
        checkNotNull(game)
        game.isGameOver =true

        //Trigger refresh to tell the GUI that the turn has ended and shows the ResultGameScene (not yet implemented)
        onAllRefreshables { refreshAfterEndGame() }
    }

    /**
     * Creates a not-shuffled standard deck (52 cards) by creating all values for each suit (clubs, spades, hearts,diamonds)
     * We keep it not-shuffled at first for testing purposes but the Standard-deck gets shuffled in the [startNewGame]
     *
     * @return a not-shuffled card stack
     */
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

