package gui
import entity.Card
interface Refreshable {

    fun refreshAfterGameStart(){}


    fun refreshAfterTurnStart(){}


    fun refreshAfterTurnEnds(){}


    fun refreshAfterCardPlayed(playedCard: Card){}


    fun refreshAfterDrawCard(drawnCard: Card){}


    fun refreshAfterCardSwap(trioCard: Card, handCard: Card){}


    fun refreshAfterDiscardCard(handCard: Card){}


    fun refreshAfterEndGame(){}

}
