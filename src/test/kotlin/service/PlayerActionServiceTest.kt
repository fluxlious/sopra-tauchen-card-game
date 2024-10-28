package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class PlayerActionServiceTest {
    private lateinit var rootService: RootService

    @BeforeEach
    fun setup() {
        rootService = RootService()

        rootService.GameService.startNewGame(listOf("Player 1", "Player 2"))
    }

    @Test
    fun testPlayCardWithValueTrio() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService

        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.CLUBS, CardValue.TEN)

        currentPlayer.hand.clear()
        currentPlayer.hand.add(compatibleCard)


        playerActionService.playCard(compatibleCard)


        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)
        assertNotEquals(5,currentPlayer.score)
        assertEquals(20,currentPlayer.score)
        println(currentPlayer.scoringPile)
        assertEquals(3, currentPlayer.scoringPile.size)
    }
    @Test
    fun testPlayCardWithSuitTrio() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService

        // Create a middle card in the game
        val middleCard = listOf(Card(CardSuit.SPADES, CardValue.TEN),
            Card(CardSuit.SPADES, CardValue.THREE),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.SPADES, CardValue.FOUR)

        currentPlayer.hand.clear()  // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)


        playerActionService.playCard(compatibleCard)


        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)
        assertNotEquals(20,currentPlayer.score)
        assertEquals(5,currentPlayer.score)
        println(currentPlayer.scoringPile)
        assertEquals(3, currentPlayer.scoringPile.size)
    }
    @Test
    fun testPlayCardOnEmptyMiddle() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService



        playerActionService.playCard(currentPlayer.hand.random())


        assertEquals(1, game.middleCards.size)
        assertEquals(4,currentPlayer.hand.size)
        assertEquals(0,currentPlayer.score)
    }
    @Test
    fun testIllegalPlay() {
        val game = rootService.GameService.game
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService

        currentPlayer.hand.clear()
        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN))
        game.middleCards.addAll(middleCard)
        println(game.middleCards)
        val incompatibleCard = Card(CardSuit.CLUBS, CardValue.TWO)
        currentPlayer.hand.add(incompatibleCard)
        print(currentPlayer.hand)

        // Assert that playing the incompatible card throws an IllegalArgumentException
        assertFailsWith<IllegalArgumentException>("Invalid play: Card must match either suit or value of a middle card") {
            playerActionService.playCard(incompatibleCard)
        }

        assertEquals(2, game.middleCards.size, "Middle cards should remain unchanged")
        assertTrue(currentPlayer.hand.contains(incompatibleCard), "The card should still be in the player's hand")
    }
    @Test
    fun testPlayCardWithFullMiddleCards() {
        val game = rootService.GameService.game
        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService

        // Set up middleCards to be full
        game.middleCards.addAll(listOf(
            Card(CardSuit.HEARTS, CardValue.TEN),
            Card(CardSuit.SPADES, CardValue.TEN),
            Card(CardSuit.DIAMONDS, CardValue.TEN)
        ))

        val extraCard = Card(CardSuit.CLUBS, CardValue.TWO)
        currentPlayer.hand.add(extraCard)


    }
    @Test
    fun testDiscardCardSuccessful(){

        val game = rootService.GameService.game
        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService
        val randomCardfromHand = currentPlayer.hand.random()
        playerActionService.discardCard(randomCardfromHand)
        assertFalse(currentPlayer.hand.contains(randomCardfromHand))
        assertTrue(game.discardPile.contains(randomCardfromHand))
    }
    @Test
    fun testDiscardCardUnsuccessful(){

        val game = rootService.GameService.game
        val currentPlayer = game.players[game.currentPlayerIndex]
        val playerActionService = rootService.PlayerActionService
        //TODO testDiscardCardUnsuccessful bitmedi daha bi de testler asiri karisti onlari toparla
    }

    }
