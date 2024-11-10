package service

import org.junit.jupiter.api.Test
import kotlin.test.*
import entity.*
import java.util.*
/**
 * Class for testing playLastCard.
 */
class PlayLastCardTest {
    private var rootService = RootService()

    /**
     * Sets up a new game before each test with [TestRefreshable]s attached to a newly created [RootService].
     * Starts a new game before each test with two players (Alice,Bob)
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        rootService.gameService.startNewGame(listOf("Alice", "Bob" ))


    }
    /**
     * Tests playing the last card and ending the game
     */
    @Test
    fun playLastCard() {
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        //clear the draw pile and put controlled card to check if the drawing last and forming trio works
        game.drawPile.clear()
        val lastCard = Card(CardSuit.CLUBS,CardValue.SEVEN)
        game.drawPile.push(lastCard)

        game.middleCards.add(Card(CardSuit.HEARTS, CardValue.SEVEN))
        game.middleCards.add(Card(CardSuit.SPADES, CardValue.SEVEN))
        rootService.playerActionService.drawCard()

        //Checks if drawCard triggers playLastCard correctly and calls the endGame function
        assertTrue (game.middleCards.isEmpty() && currentPlayer.score == 20 && game.isGameOver)
    }
}