package entity

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
/**
 * Unit tests for the [Tauchen] class, covering default initialisations, player switching
 * and operations with card piles
 */
class TauchenTest {
    private val testGame = Tauchen(0, listOf(Player("Cem"),Player("Can")))

    /**
     * Tests the initial state of the [Tauchen] instance
     */

    @Test
    fun testGameInitialization(){
        //Test the Tauchen's properties after game initialization
        assertEquals(0,testGame.currentPlayer)
        assertEquals(2,testGame.players.size)
        assertTrue(testGame.drawPile.isEmpty(), "Draw pile should be empty initially")
        assertTrue(testGame.middleCards.isEmpty(), "Middle cards should be empty initially")
        assertTrue(testGame.discardPile.isEmpty(), "Discard pile should be empty initially")

    }

    /**
     * Tests manually switching player by updating the [Tauchen.currentPlayer]
     */
    @Test
    fun testPlayerSwitching(){

        assertEquals(0, testGame.currentPlayer)

        // Switch to next player
        testGame.currentPlayer = (testGame.currentPlayer + 1) % testGame.players.size
        assertEquals(1, testGame.currentPlayer)

        // Switch back to the first player
        testGame.currentPlayer= (testGame.currentPlayer + 1) % testGame.players.size
        assertEquals(0, testGame.currentPlayer)


    }
    /**
     * Tests manually adding a [Card] to [Tauchen.middleCards]
     */
    @Test
    fun testMovingCardToMiddle(){
        val aceOfHearts = Card(CardSuit.HEARTS,CardValue.ACE)

        testGame.middleCards.add(aceOfHearts)
        assertEquals(1,testGame.middleCards.size, "Middle cards should contain 1 card")
        assertEquals(aceOfHearts,testGame.middleCards[0], "The first card in the middle should be aceOfHearts")

    }

    /**
     * Tests manually adding and removing a [Card] in the [Tauchen.discardPile]
     */
    @Test
    fun testMovingCardToDiscardPile(){
        val kingOfSpades = Card(CardSuit.SPADES, CardValue.KING)

        testGame.discardPile.push(kingOfSpades)
        assertEquals(1,testGame.discardPile.size, "Discard pile should contain 1 card after push")
        assertEquals(kingOfSpades,testGame.discardPile.peek(), "Top of the DiscardPile should be kingOfSpades")
        testGame.discardPile.pop()

        assertTrue(testGame.discardPile.isEmpty(), "Discard pile should be empty after pop")

    }
}