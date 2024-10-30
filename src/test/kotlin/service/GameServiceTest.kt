package service

import kotlin.test.*


class GameServiceTest{
    private val service = GameService(RootService())
    @Test
    fun testStartNewGame(){
        service.startNewGame(listOf("Cem", "Can"))
        //Checks if the players list is populated with the given names
        assertEquals(service.game.players.size, 2)

        //Checks if the both names are correct
        assertEquals("Cem", service.game.players[0].name)
        assertEquals("Can", service.game.players[1].name)


        //Checks if the drawPile is shuffled by comparing with a standard deck
        assertNotEquals(service.game.drawPile, service.createStandardDeck())

        //Checks if both players have 5 cards in the hand
        service.game.players.forEach { player ->
            assertEquals(5, player.hand.size, "Each player should have exactly 5 cards in their hand")
            println(player.hand)
        }
        println(service.game.drawPile)
        //Checks if the drawPile has 42 cards after the cards are dealt
        assertEquals(42, service.game.drawPile.size)
        //Checks the boundary of currentPlayerIndex either 0 or 1
        assertTrue(service.game.currentPlayerIndex == 0  || service.game.currentPlayerIndex == 1)

        assertTrue(service.game.players.all { player -> player.scoringPile.isEmpty() })

    }



}