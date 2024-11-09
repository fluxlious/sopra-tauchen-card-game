package service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

/**
 * Class for testing multiple different methods of [GameService]
 */
class GameServiceTest{
    private var rootService = RootService()


    @BeforeTest
    fun setUp() {
        rootService = RootService()
        rootService.gameService.startNewGame(listOf("Alice", "Bob"))
    }

    /** Tests starting the game with invalid name entries */
    @Test
    fun testInvalidPlayerNames(){

        //Checks the unaccepted name entries
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

    /** Tests the game and player properties if they are set correct after the game starts */
    @Test
    fun testInitialGameState(){
        val game = rootService.currentGame
        assertNotNull(game)

        //Checks if the players list is populated with the given names
        assertEquals(game.players.size, 2)

        //Checks if both players have 5 cards in the hand and scoringPile are empty initially
        game.players.forEach { player ->
            assertEquals(5, player.hand.size, "Each player should have exactly 5 cards in their hand")
            assertTrue(player.scoringPile.isEmpty())
            println(player.hand)
        }


        //Checks if the drawPile has 42 cards after the cards are dealt
        assertEquals(42, game.drawPile.size)

        //Checks the boundary of currentPlayerIndex either 0 or 1
        assertTrue(game.currentPlayerIndex == 0  || game.currentPlayerIndex == 1)



    }
    /** Tests starting a new game and ends the turn */
    @Test
    fun testStartNewGameAndEndTurn() {
        val game = rootService.currentGame
        assertNotNull(game)

        //Checks the game initialisation properties
        assertEquals(2, game.players.size)
        assertEquals("Alice", game.players[0].name)
        assertEquals("Bob", game.players[1].name)

        //Normally this value is random after the game starts but here controlled
        game.currentPlayerIndex = 0

        //Checks if the game ends correctly
        assertDoesNotThrow { rootService.gameService.endTurn() }

        //Checks if the game index tracker set to the next player
        assertEquals(1, game.currentPlayerIndex)

    }
    /** Tests starting a turn with no game active. */
    @Test
    fun testStartTurnNoGame() {
        rootService.currentGame = null

        //Tests if the turn starts when the game is null
        assertThrows<IllegalStateException> { rootService.gameService.startTurn() }
    }

    /** Tests ending a turn with no game active. */
    @Test
    fun testEndTurnNoGame() {
        rootService.currentGame = null
        //Tests if the turn ends when the game is null
        assertThrows<IllegalStateException> { rootService.gameService.endTurn() }
    }

}