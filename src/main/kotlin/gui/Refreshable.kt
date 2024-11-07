package gui
import entity.Card
interface Refreshable {

    fun refreshAfterStartNewGame(){}


    fun refreshAfterTurnStart(){}


    fun refreshAfterTurnEnd(){}


    fun refreshAfterPlayCard(playedCard: Card){}


    fun refreshAfterDrawCard(drawnCard: Card){}


    fun refreshAfterCardSwap(trioCard: Card, handCard: Card){}


    fun refreshAfterDiscardCard(handCard: Card){}


    fun refreshAfterEndGame(){}

}
