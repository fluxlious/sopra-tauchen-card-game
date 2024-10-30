package entity

import java.util.*

/**
 * Entity to represent a player in the game "Tauchen"
 * A player has a name, a score, and various states and collections
 *
 * @constructor initializes the player with a [name]
 * @param name the name of the player
 * @property score the score of the player
 * @property hasSwapped indicates if the player has swapped a card in the current round
 * @property hasActionTaken indicates whether the player has taken any game actions in the current round
 * @property hand the mutable list of cards that the player holds currently
 * @property swapCard the card that the player takes from the middle during swap action
 * @property scoringPile the list of trios that the player has formed
 */
class Player (var name : String){
    var score : Int = 0

    var hasSwapped : Boolean = false
    //var hasActionTaken : Boolean = false

    val hand: MutableList<Card> = mutableListOf()
    var swapCard: Card? = null
    val scoringPile: MutableList<List<Card>> = mutableListOf()

    init {
        require(score >= 0) { "Score must be greater or equal to 0" }
    }

}

