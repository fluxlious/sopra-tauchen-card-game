package entity

/**
 * Data class for the game entity [Card]
 * It is characterized by a [CardSuit] and a [CardValue]
 */
data class Card (val suit: CardSuit, val value: CardValue){

    override fun toString() = "$suit$value"
}