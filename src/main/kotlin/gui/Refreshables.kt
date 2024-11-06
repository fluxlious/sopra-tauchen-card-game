package gui
import entity.Card
interface Refreshables {

    fun refreshAfterGameStart(){}


    fun refreshAfterTurnStart(){}


    fun refreshAfterTurnEnds(){}


    fun refreshAfterCardPlayed(playedCard: Card){}


    fun refreshAfterCardDrawn(card: Card){}


    fun refreshAfterCardSwap(trioCard: Card, handCard: Card){}


    fun refreshAfterCardDiscarded(handCard: Card){}


    fun refreshAfterGameEnds(){}

}
