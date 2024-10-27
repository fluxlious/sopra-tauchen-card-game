package entity

import org.junit.jupiter.api.Assertions.*
import kotlin.test.*
/**
 * Unit tests for the [Player] class, covering initialisations and default properties
 */
class PlayerTest{
    //A test player
    private val testPlayer = Player("Alex")

    /**
     * Tests the initial state of the [Player] instance
     */
    @Test
    fun testPlayerInitialization(){
        assertEquals("Alex", testPlayer.name)
        assertEquals(0, testPlayer.score)
        assertEquals(false, testPlayer.hasSwapped)
        assertEquals(false,testPlayer.hasActionTaken)
        assertEquals(null, testPlayer.swapCard)
        assertTrue(testPlayer.hand.isEmpty())
        assertTrue(testPlayer.scoringPile.isEmpty())

    }



}