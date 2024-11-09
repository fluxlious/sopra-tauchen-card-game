package service

import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class PlayerActionServiceTest {
    private var rootService = RootService()
//    private var testRefreshable = TestRefreshable(rootService)


    @BeforeTest
    fun setUp() {
        rootService = RootService()
        //testRefreshable = TestRefreshable(rootService)
        //rootService.addRefreshable(testRefreshable)
        rootService.gameService.startNewGame(listOf("Alice", "Bob" ))
    }



    //Tests playing a card that forms a value trio
    @Test
    fun testPlayCardWithValueTrio() {
        val game = rootService.currentGame
        assertNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.CLUBS, CardValue.TEN)

        currentPlayer.hand.clear() // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)

        println("Player has ${currentPlayer.hand} before trio ")
        println("Middle card has ${game.middleCards} before trio ")
        rootService.playerActionService.playCard(compatibleCard)
        println("Player has ${currentPlayer.hand} post trio ")

        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)//post-trio middle should be empty
        assertEquals(20,currentPlayer.score)//post-trio score should be 20
        assertEquals(1, currentPlayer.scoringPile.size)//post-trio scoringPile should have one trio
    }
    //Tests playing a card that forms a suit trio
    @Test
    fun testPlayCardWithSuitTrio() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]


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
        assertEquals(5,currentPlayer.score)
        assertEquals(1, currentPlayer.scoringPile.size)
    }
    //Tests playing a card that on an empty middle
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
        assertNotNull(game)
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
        assertNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]

        val randomFiveCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),
                                            Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))

        currentPlayer.hand.addAll(randomFiveCards)
        val discardedCard = Card(CardSuit.HEARTS, CardValue.THREE)
        rootService.playerActionService.discardCard(discardedCard)
        assertFalse(currentPlayer.hand.contains(discardedCard))
        assertTrue(game.discardPile.contains(discardedCard))
    }
    @Test
    fun testDiscardCardUnsuccessful(){
        val game = rootService.currentGame
        assertNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]
        currentPlayer.hand.clear() // Clear hand for controlled setup


        val randomFiveCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),
            Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))

        currentPlayer.hand.addAll(randomFiveCards)
        val discardedCard = Card(CardSuit.HEARTS, CardValue.TEN) //player doesn't have this card in the hand.
        assertThrows<IllegalArgumentException>(){rootService.playerActionService.discardCard(discardedCard) }
    }
   @Test
   fun testDrawAndPlayCardOnTwoCardsMiddle(){
       val game = rootService.currentGame
       checkNotNull(game)
       val currentPlayer = game.players[game.currentPlayerIndex]

       //Setting the controlled setup
       val middleCard = listOf(Card(CardSuit.SPADES, CardValue.TEN),
           Card(CardSuit.SPADES, CardValue.THREE),)
       game.middleCards.addAll(middleCard)
       game.drawPile.push(Card(CardSuit.SPADES, CardValue.THREE))

       rootService.playerActionService.drawCard()
       //checks if the drawing action is successful
       assertTrue(currentPlayer.hand.size == 6)
       rootService.playerActionService.playCard(currentPlayer.hand.last())
       assertTrue(game.middleCards.isEmpty() && currentPlayer.score == 5 )


   }
    @Test
    fun testDrawCardPlayCardOnEmptyMiddle(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]


        rootService.playerActionService.drawCard()
        println(game.middleCards)
        println(currentPlayer.hand)
        rootService.playerActionService.playCard(currentPlayer.hand.last())

        println("After draw and play")
        println(game.middleCards)
        println(currentPlayer.hand)

    }

    @Test
    fun testNextTurnWithOutActionTaken(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]
        assertThrows<IllegalStateException> { rootService.playerActionService.nextTurn() }

    }
    @Test
    fun testSwapCardSuccessful(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        currentPlayer.hand.clear()
        val controlledMiddleCard = listOf(Card(CardSuit.HEARTS, CardValue.SEVEN),
                                Card(CardSuit.SPADES, CardValue.SEVEN),)

        game.middleCards.addAll(controlledMiddleCard)
        currentPlayer.hand.add(Card(CardSuit.HEARTS, CardValue.TEN))
        // Before Swap Hand: [♥10], Middle: [♥7, ♠7]
        rootService.playerActionService.swapCard(currentPlayer.hand.last(), game.middleCards[1])

        // After Swap Hand: [♠7], Middle: [♥7, ♥10]
        //After swap action, check if both cards on the right place
        assertTrue(currentPlayer.hand.contains(Card(CardSuit.SPADES, CardValue.SEVEN)))
        assertTrue(game.middleCards.contains(Card(CardSuit.HEARTS, CardValue.TEN)))
        assertTrue(currentPlayer.hasActionTaken && currentPlayer.hasSwapped)
    }
    @Test
    fun testSwapCardUnsuccessful(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        currentPlayer.hand.clear()
        val controlledMiddleCard = listOf(Card(CardSuit.DIAMONDS, CardValue.EIGHT),
            Card(CardSuit.SPADES, CardValue.EIGHT),)

        game.middleCards.addAll(controlledMiddleCard)
        currentPlayer.hand.add(Card(CardSuit.HEARTS, CardValue.TEN))


        assertThrows<IllegalArgumentException> { rootService.playerActionService.swapCard(currentPlayer.hand.last(), game.middleCards[1])}

        assertFalse(currentPlayer.hasActionTaken && currentPlayer.hasSwapped)
    }




}
