package entity

import java.util.*


class Player (var name : String,
              var score : Int = 0,

              var hasSwapped : Boolean = false,
              var hasActionTaken : Boolean = false,

              var hand: List<Card> = mutableListOf(),
              var swapCard: Card? = null,
              var scoringPile : Stack<Card> = Stack()
)
