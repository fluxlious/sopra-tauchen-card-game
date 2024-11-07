package entity

import kotlin.test.*
/**
 * Unit tests for the [Card] class, covering initialisations and properties of the [Card]
 * and equality checks of two [Card]s
 */

class CardTest{

    //Some cards to perform tests with
    private val aceOfDiamonds = Card(CardSuit.DIAMONDS,CardValue.ACE)
    private val otherAceOfDiamonds = Card(CardSuit.DIAMONDS,CardValue.ACE)
    private val kingOfDiamonds = Card(CardSuit.DIAMONDS,CardValue.KING)

    /**
     * Tests the properties of the [Card] instance by accessing its suit and value directly
     */
    @Test
    fun testCardInitializationByValue() {
        assertEquals(CardSuit.DIAMONDS, aceOfDiamonds.suit)
        assertEquals(CardValue.ACE, aceOfDiamonds.value)

    }

    /**
     * Tests the properties of the [Card] instance by comparing [toString] representations
     */
    @Test
    fun testCardInitializationUsingToString(){
        assertEquals("♦",aceOfDiamonds.suit.toString())
        assertEquals("A",aceOfDiamonds.value.toString())
    }

    /**
     * Tests the equality of two [Card] by [toString] representations
     */
    @Test
    fun testCardEqualityUsingToString(){
        assertEquals(aceOfDiamonds.toString(),otherAceOfDiamonds.toString())
    }

    /**
     * Tests the inequality of two [Card] by [toString] representations
     */
    @Test
    fun testCardsInequalityUsingToString(){
        assertNotEquals(aceOfDiamonds.toString(),kingOfDiamonds.toString())
    }

}