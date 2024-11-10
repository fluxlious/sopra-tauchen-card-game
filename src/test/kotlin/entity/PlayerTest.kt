package entity

import kotlin.test.*
/**
 * Unit tests for the [Player] class, covering initialisations and properties
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
        //assertEquals(false,testPlayer.hasActionTaken)
        assertTrue(testPlayer.hand.isEmpty())
        assertTrue(testPlayer.scoringPile.isEmpty())

    }



}