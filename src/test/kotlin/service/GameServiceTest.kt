package service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.assertThrows
import kotlin.test.*


class GameServiceTest{

    private var rootService = RootService()


    @BeforeTest
    fun setUp() {
        rootService = RootService()
        rootService.gameService.startNewGame(listOf("Alice", "Bob"))
    }

    @Test
    fun testInvalidPlayerNames(){
        assertFailsWith<IllegalArgumentException>{
            rootService.gameService.startNewGame(listOf("Alice", "Bob", "Unwanted"))
        }
        assertFailsWith<IllegalArgumentException>{
            rootService.gameService.startNewGame(listOf("Alice", "Alice"))
        }
        assertFailsWith<IllegalArgumentException>{
            rootService.gameService.startNewGame(listOf("","Bob"))
        }

    }
    @Test
    fun testInitialGameState(){
        val game = rootService.currentGame
        assertNotNull(game)
        //Checks if the players list is populated with the given names
        assertEquals(game.players.size, 2)

        //Checks if the both names are correct
        assertEquals("Alice", game.players[0].name)
        assertEquals("Bob", game.players[1].name)

        //Checks if both players have 5 cards in the hand and scoringPile are empty
        game.players.forEach { player ->
            assertEquals(5, player.hand.size, "Each player should have exactly 5 cards in their hand")
            assertTrue(player.scoringPile.isEmpty())
            println(player.hand)
        }

        println(game.drawPile)

        //Checks if the drawPile has 42 cards after the cards are dealt
        assertEquals(42, game.drawPile.size)

        //Checks the boundary of currentPlayerIndex either 0 or 1
        assertTrue(game.currentPlayerIndex == 0  || game.currentPlayerIndex == 1)

        assertTrue(game.players.all { player -> player.scoringPile.isEmpty() })


    }

    @Test
    fun testStartNewGame() {
        val game = rootService.currentGame
        assertNotNull(game)

        // Test: The game has been created with the correct players
        assertEquals(2, game.players.size)
        assertEquals("Alice", game.players[0].name)
        assertEquals("Bob", game.players[1].name)

        val previousPlayerIndex = game.players[game.currentPlayerIndex]
        // Test: Calling endTurn will also call startTurn
        assertDoesNotThrow { rootService.gameService.endTurn() }

        assertNotEquals(previousPlayerIndex, game.players[game.currentPlayerIndex])

    }
    /** Test starting a turn with no game active. */
    @Test
    fun testStartTurnNoGame() {
        // Set the current game of the root service to null
        rootService.currentGame = null

        // Test: No game is currently active
        assertThrows<IllegalStateException> { rootService.gameService.startTurn() }
    }

    /** Test ending a turn with no game active. */
    @Test
    fun testEndTurnNoGame() {
        // Set the current game of the root service to null
        rootService.currentGame = null

        // Test: No game is currently active
        assertThrows<IllegalStateException> { rootService.gameService.endTurn() }
    }
}