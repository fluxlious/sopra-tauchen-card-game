package entity

import java.util.*

class Tauchen(var currentPlayerIndex: Int = 0,
              val drawPile: Stack<Card> = Stack(),
              var middleCards: MutableList<Card> = mutableListOf(),
              var discardPile: Stack<Card> = Stack()
)
