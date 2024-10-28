package entity

import java.util.*

/**
 * Entity to represent the game itself "Tauchen"
 * This class manages the core game entity including the list of players, card piles and tracking the current player
 *
 * @constructor creates a game with the given two players
 * @property currentPlayer tracks which player's turn it is
 * @property players the list of the two players
 * @property drawPile the stack of cards where players can draw cards
 * @property middleCards the list of the cards in the middle, used to form trios or be swapped.
 * @property discardPile stores the discarded cards
 */
class Tauchen(var currentPlayerIndex: Int = 0, val players : List<Player>) {

    val drawPile: Stack<Card> = Stack()
    val middleCards: MutableList<Card> = mutableListOf()
    val discardPile: Stack<Card> = Stack()

}


