package entity

import java.util.*
/**
 * Entity to represent a player in the game "Tauchen"
 * A player has a name, a score, and various states and collections
 *
 * @constructor initializes the player with a [name]
 * @property name the name of the player, initialized through the constructor
 * @property score the score of the player
 * @property hasSwapped indicates if the player has swapped a card in the current round
 * @property hasActionTaken indicates whether the player has taken any game actions in the current round
 * @property hand the mutable list of cards that the player holds currently
 * @property swapCard the card that the player takes from the middle during swap action
 * @property scoringPile the sets of trios that the player has formed
 */
class Player (var name : String){
    var score : Int = 0

    var hasSwapped : Boolean = false
    var hasActionTaken : Boolean = false

    var hand: MutableList<Card> = mutableListOf()
    var swapCard: Card? = null
    var scoringPile : Stack<Card> = Stack()

}

