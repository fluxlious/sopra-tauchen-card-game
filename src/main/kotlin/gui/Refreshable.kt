package gui
import entity.Card
import entity.Player

/**
 * This interface allows service layer classes to communicate with the GUI
 * by refreshing GUI component according to the certain changes in the game.
 */
interface Refreshable {

    /** Refreshes the GUI after a new game has been started. */
    fun refreshAfterStartNewGame(){}

    /** Refreshes the GUI after a new turn has been started. */
    fun refreshAfterStartTurn(){}

    /** Refreshes the GUI after a turn has ended */
    fun refreshAfterTurnEnd(){}

    /** Refreshes the GUI after a card has been played.
     *
     * @param playedCard the [Card] has been played
     */
    fun refreshAfterPlayCard(playedCard: Card){}

    //TODO add kdoc for refreshAfterTakeTrio
    fun refreshAfterTakeTrio(){}
    /** Refreshes the GUI after a card has been played.
     *
     * @param drawnCard the [Card] has been drawn
     */
    fun refreshAfterDrawCard(drawnCard: Card){}

    /** Refreshes the GUI after a swap action has taken place
     *
     * @param cardFromHand the [Card] that is selected from the hand for swap action
     * @param cardFromMiddle the [Card] that is selected from the middle for swap action
     */
    fun refreshAfterSwapCard(cardFromHand : Card, cardFromMiddle : Card){}

    /** Refreshes the GUI after a card has been drawn from drawStack
     *
     * @param discardedCard the [Card] has been discarded.
     */
    fun refreshAfterDiscardCard(discardedCard : Card){}

    /** Refreshes the GUI and pops a prompt that the player need to discard before ending the turn
     *
     * @param currentPlayer the player that the prompt will be shown
     */
    fun refreshAfterDiscardPrompt(currentPlayer : Player){
    }

    /** Refreshes the GUI after the game has ended */
    fun refreshAfterEndGame(){}

    /** Refreshes the GUI after the game has been restarted. */
    fun refreshAfterGameRestart() {}


}
