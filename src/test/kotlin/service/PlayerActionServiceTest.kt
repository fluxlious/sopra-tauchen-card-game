package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class PlayerActionServiceTest {
    private lateinit var rootService: RootService

    @BeforeEach
    fun setup() {
        rootService = RootService()

        // Start a new game with two players through GameService in RootService
        rootService.GameService.startNewGame(listOf("Player 1", "Player 2"))
    }

    @Test
    fun testPlayCardWithValueTrio() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService

        // Create a middle card in the game
        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.CLUBS, CardValue.TEN)

        currentPlayer.hand.clear()  // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)


        // Play the compatible card
        playerActionService.playCard(compatibleCard)


        // Assertions
        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)
        assertNotEquals(5,currentPlayer.score)
        assertEquals(20,currentPlayer.score)
    }
    @Test
    fun testPlayCardOnEmptyMiddle() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService



        // Play the compatible card
        playerActionService.playCard(currentPlayer.hand.random())


        assertEquals(1, game.middleCards.size)
        assertEquals(4,currentPlayer.hand.size)
        assertEquals(0,currentPlayer.score)
    }

}