package service

import org.junit.jupiter.api.Test
import kotlin.test.*
import entity.*
import java.util.*

class PlayLastCardTest {
    private var rootService = RootService()
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        rootService.gameService.startNewGame(listOf("Alice", "Bob" ))


    }
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

        assertTrue { game.middleCards.isEmpty() && currentPlayer.score == 20}
    }
}