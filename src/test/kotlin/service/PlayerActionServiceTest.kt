package service

import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.*
/**
* Class for testing multiple different methods of the [PlayerActionService].
*/
class PlayerActionServiceTest {
    private var rootService = RootService()
//    private var testRefreshable = TestRefreshable(rootService)

    /**
     * Sets up a new game before each test with [TestRefreshable]s attached to a newly created [RootService].
     * Starts a new game before each test with two players (Alice,Bob)
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        //testRefreshable = TestRefreshable(rootService)
        //rootService.addRefreshable(testRefreshable)
        rootService.gameService.startNewGame(listOf("Alice", "Bob" ))
    }
    /** Tests playing a card that forms a value trio*/
    @Test
    fun testPlayCardWithValueTrio() {
        val game = rootService.currentGame
        assertNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        //Set the controlled middle and hand for the test
        val middleCard = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.CLUBS, CardValue.TEN)

        currentPlayer.hand.clear() // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)

        rootService.playerActionService.playCard(compatibleCard)

        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)//post-trio middle should be empty
        assertEquals(20,currentPlayer.score)//post-trio score should be 20
        assertEquals(1, currentPlayer.scoringPile.size)//post-trio scoringPile should have one trio
    }
    /** Tests playing a card that forms a suit trio*/
    @Test
    fun testPlayCardWithSuitTrio() {
        val game = rootService.currentGame
        assertNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        //Set the controlled middle and hand for the test
        val middleCard = listOf(Card(CardSuit.SPADES, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.THREE),)

        game.middleCards.addAll(middleCard)
        val compatibleCard = Card(CardSuit.SPADES, CardValue.FOUR)

        currentPlayer.hand.clear()  // Clear hand for controlled setup
        currentPlayer.hand.add(compatibleCard)

        rootService.playerActionService.playCard(compatibleCard)

        //Checks the post-trio state
        assertFalse(currentPlayer.hand.contains(compatibleCard), "The card should be removed from player's hand")
        assertEquals(0, game.middleCards.size)
        assertEquals(5,currentPlayer.score)
        assertEquals(1, currentPlayer.scoringPile.size)
    }
    /** Tests playing a card on an empty middle */
    @Test
    fun testPlayCardOnEmptyMiddle() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]


        rootService.playerActionService.playCard(currentPlayer.hand.random())

        //Checks the post-trio state
        assertEquals(1, game.middleCards.size)
        assertEquals(4,currentPlayer.hand.size)
        assertEquals(0,currentPlayer.score)

    }
    /** Tests playing an invalid card */
    @Test
    fun testIllegalPlay() {
        val game = rootService.currentGame
        assertNotNull(game, "Game should be initialized")
        val currentPlayer = game.players[game.currentPlayerIndex]

        //Set the controlled middle and hand for the test
        val middleCardTest = listOf(Card(CardSuit.HEARTS, CardValue.TEN),
                                Card(CardSuit.SPADES, CardValue.TEN))
        game.middleCards.addAll(middleCardTest)

        currentPlayer.hand.clear()
        val incompatibleCard = Card(CardSuit.CLUBS, CardValue.TWO)
        currentPlayer.hand.add(incompatibleCard)

        // Assert that playing the incompatible card throws an IllegalArgumentException
        assertFailsWith<IllegalArgumentException>("Invalid play: Card must match either suit or value of a middle card") {
            rootService.playerActionService.playCard(incompatibleCard)
        }

        //Checks the state after the player couldn't play the card
        assertEquals(2, game.middleCards.size, "Middle cards should remain unchanged")
        assertTrue(currentPlayer.hand.contains(incompatibleCard), "The card should still be in the player's hand")
    }
    /** Tests discarding a card*/
    @Test
    fun testDiscardCardSuccessful(){
        val game = rootService.currentGame
        assertNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        val randomFourCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),
                                            Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))

        //Now the player has 9 cards, so needs to discard a card
        currentPlayer.hand.addAll(randomFourCards)
        val discardedCard = Card(CardSuit.HEARTS, CardValue.THREE)

        rootService.playerActionService.discardCard(discardedCard)

        //Checks the post-discardCard state
        assertFalse(currentPlayer.hand.contains(discardedCard))
        assertTrue(game.discardPile.contains(discardedCard))
    }
    /** Tests discarding a card*/
    @Test
    fun testDiscardCardUnsuccessful(){
        val game = rootService.currentGame
        assertNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]
        currentPlayer.hand.clear()

        val randomFourCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),
            Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))

        currentPlayer.hand.addAll(randomFourCards)
        val discardedCard = Card(CardSuit.HEARTS, CardValue.TEN) //player doesn't have this card in the hand.
        assertThrows<IllegalArgumentException>{ rootService.playerActionService.discardCard(discardedCard) }
    }
    /** Tests the scenario that if the player has 9 cards before the turn ends */
    @Test
    fun testNextTurn9CardsInHand(){
        val game = rootService.currentGame
        assertNotNull(game)

        val currentPlayer = game.players[game.currentPlayerIndex]

        val randomFourCards = mutableListOf(Card(CardSuit.HEARTS, CardValue.THREE),Card(CardSuit.SPADES, CardValue.TWO),
            Card(CardSuit.SPADES, CardValue.TEN),Card(CardSuit.SPADES, CardValue.NINE))
        currentPlayer.hasActionTaken = true

        currentPlayer.hand.addAll(randomFourCards)
        rootService.playerActionService.nextTurn()

        //Check if the player has 8 card after the forced discard.
        assertEquals(8, currentPlayer.hand.size)

        //Checks discarded goes into discardPile
        assertTrue(game.discardPile.contains(Card(CardSuit.SPADES, CardValue.NINE)))
    }

    /** Tests drawing a card, playing the drawn card and forming a trio*/
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

       //Checks if the player has 6 cards after drawing
       assertTrue(currentPlayer.hand.size == 6)

       rootService.playerActionService.playCard(currentPlayer.hand.last())

        //Checks there are no cards in the middle, score is set and scoringPile has the trio.
       assertTrue(game.middleCards.isEmpty() && currentPlayer.score == 5 && currentPlayer.scoringPile.isNotEmpty())

   }
    @Test
    fun testDrawAndTryPlayAnotherCard(){
        val game = rootService.currentGame
        checkNotNull(game)
        val currentPlayer = game.players[game.currentPlayerIndex]

        //Setting the controlled setup
        currentPlayer.hand.clear()
        val middleCard = listOf(Card(CardSuit.SPADES, CardValue.TEN),
            Card(CardSuit.SPADES, CardValue.THREE),)
        currentPlayer.hand.add(Card(CardSuit.SPADES, CardValue.TWO))
        game.middleCards.addAll(middleCard)
        game.drawPile.push(Card(CardSuit.DIAMONDS, CardValue.THREE))

        rootService.playerActionService.drawCard()
        //cannot draw again
        assertThrows<IllegalStateException> {rootService.playerActionService.drawCard()}

        //Checks if the player has 6 cards after drawing
        assertTrue(currentPlayer.hand.size == 2)
        assertTrue(currentPlayer.hand.contains(Card(CardSuit.DIAMONDS, CardValue.THREE) ))

        //the player tries to play another card in the hand that forms normally trio but not the drawn card
        assertThrows<IllegalStateException> {rootService.playerActionService.playCard(Card(CardSuit.SPADES, CardValue.TWO))  }


    }
    /** Tests changing the turn without taking any action*/
    @Test
    fun testNextTurnWithOutActionTaken(){
        val game = rootService.currentGame
        checkNotNull(game)

        //Test allowing to proceed into next Turn without any action taken.
        assertThrows<IllegalStateException> { rootService.playerActionService.nextTurn() }

    }
    /** Tests swapping a card that is valid */
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
    /** Tests swapping a card that is invalid */
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

        //Checks that the invalid swap throws exception
        assertThrows<IllegalArgumentException> { rootService.playerActionService.swapCard(currentPlayer.hand.last(), game.middleCards[1])}

        assertFalse(currentPlayer.hasActionTaken && currentPlayer.hasSwapped)
    }

}
