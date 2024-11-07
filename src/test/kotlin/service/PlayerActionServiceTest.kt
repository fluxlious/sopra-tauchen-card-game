package service

import entity.*
import kotlin.test.*

class PlayerActionServiceTest {
    private var rootService = RootService()
//    private var testRefreshable = TestRefreshable(rootService)


    @BeforeTest
    fun setUp() {
        rootService = RootService()
//        testRefreshable = TestRefreshable(rootService)
//        rootService.addRefreshable(testRefreshable)
        rootService.gameService.startNewGame(listOf("Alice", "Bob" ))
    }




    @Test
    fun testPlayCardWithValueTrio() {
        val game = rootService.currentGame
        assertNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]

        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.CLUBS, CardValue.TEN)

        currentPlayer.hand.clear()
        currentPlayer.hand.add(compatibleCard)

        println("Player has ${currentPlayer.hand} before trio ")
        println("Middle card has ${game.middleCards} before trio ")

        println("Player has ${currentPlayer.hand} post trio ")

        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)//post-trio middle should be empty
        assertEquals(20,currentPlayer.score)//post-trio score should be 20
        assertEquals(1, currentPlayer.scoringPile.size)//post-trio scoringPile should have one trio
    }
    @Test
    fun testPlayCardWithSuitTrio() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")

        val currentPlayer = game.players[game.currentPlayerIndex]

        // Create a middle card in the game
        val middleCard = listOf(Card(CardSuit.SPADES, CardValue.TEN),
            Card(CardSuit.SPADES, CardValue.THREE),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.SPADES, CardValue.FOUR)

        currentPlayer.hand.clear()  // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)


        println("Player has ${currentPlayer.hand} before trio ")
        println("Middle card has ${game.middleCards} before trio ")
        rootService.playerActionService.playCard(compatibleCard)
        println("Player has ${currentPlayer.hand} post trio ")

        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)
        assertNotEquals(20,currentPlayer.score)
        assertEquals(5,currentPlayer.score)
        println(currentPlayer.scoringPile)
        assertEquals(1, currentPlayer.scoringPile.size)
    }
    @Test
    fun testPlayCardOnEmptyMiddle() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]



        rootService.playerActionService.playCard(currentPlayer.hand.random())


        assertEquals(1, game.middleCards.size)
        assertEquals(4,currentPlayer.hand.size)
        assertEquals(0,currentPlayer.score)
    }
    @Test
    fun testIllegalPlay() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]


        currentPlayer.hand.clear()
        val middleCardTest = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN))
        //add the custom middle to the middle
        game.middleCards.addAll(middleCardTest)

        println(game.middleCards)
        val incompatibleCard = Card(CardSuit.CLUBS, CardValue.TWO)
        currentPlayer.hand.add(incompatibleCard)
        print(currentPlayer.hand)

        // Assert that playing the incompatible card throws an IllegalArgumentException
        assertFailsWith<IllegalArgumentException>("Invalid play: Card must match either suit or value of a middle card") {
            rootService.playerActionService.playCard(incompatibleCard)
        }

        assertEquals(2, game.middleCards.size, "Middle cards should remain unchanged")
        assertTrue(currentPlayer.hand.contains(incompatibleCard), "The card should still be in the player's hand")
    }
    @Test
    fun testPlayCardWithFullMiddleCards() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")


        val currentPlayer = game.players[game.currentPlayerIndex]

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

        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]

        val randomFiveCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))
        currentPlayer.hand.addAll(randomFiveCards)
        println(currentPlayer.hand.toString())
        val discardedCard = Card(CardSuit.HEARTS, CardValue.THREE)
        rootService.playerActionService.discardCard(discardedCard)
        println(currentPlayer.hand.toString())
        assertFalse(currentPlayer.hand.contains(discardedCard))
        assertTrue(game.discardPile.contains(discardedCard))
    }
}


