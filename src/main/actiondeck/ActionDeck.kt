package singletonopoly.actiondeck

import java.util.ArrayDeque
import java.util.Queue
import singletonopoly.Misc

class ActionDeck {
    private val cardsQueue: Queue<ActionCard>
    init {
        /*
        Use an array list to shuffle the cards since the Fisher-Yates shuffle, the shuffling algorithm
        used by Kotlin, accesses elements by index.
         */
        val shuffledCards: List<ActionCard> =
            arrayListOf(
                MoneyGainCard(amount = 128),
                MoneyGainCard(amount = 256),
                MoneyLossCard(amount = 128),
                MoneyLossCard(amount = 256),
                GetMoneyFromOtherPlayersCard(moneyToGetFromEachPlayer = 32),
                GetMoneyFromOtherPlayersCard(moneyToGetFromEachPlayer = 64),
                PayMoneyToOtherPlayersCard(moneyToPayEachPlayer = 32),
                PayMoneyToOtherPlayersCard(moneyToPayEachPlayer = 64),
                RestaurantMaintenanceCard(feePerRestaurant = 32),
                RestaurantMaintenanceCard(feePerRestaurant = 64),
                MoveAheadCard(numSpaces = 4),
                MoveAheadCard(numSpaces = 8),
                MoveBackCard(numSpaces = 4),
                MoveBackCard(numSpaces = 8),
                LagosAvenueCard,
                KnuthStreetCard,
                GoOnVacationCard,
                GoOnVacationCard,
                GetOffVacationFreeCard,
                GetOffVacationFreeCard,
                GetOffVacationFreeCard
            )
            .apply { shuffle() }
        
        cardsQueue = ArrayDeque(shuffledCards)
    }
    
    val topCard: ActionCard
        get() = cardsQueue.element()
    
    /**
     * Makes it so that the card that used to be the top card is now the bottom card and the card
     * that used to be 2nd from the top is now the top card.
     */
    fun moveTopCardToBottom() {
        with(cardsQueue) { add(remove()) }
    }
    
    fun removeGetOffVacationCardAtTop() {
        if (topCard is GetOffVacationFreeCard) {
            cardsQueue.remove()
        } else {
            Misc.printError(
                "The Action Deck had removeGetOffVacationCardAtTop called when the top card wasn't a " +
                "GetOffVacationFreeCard"
            )
        }
    }
    
    fun insertGetOffVacationCardAtBottom() {
        cardsQueue.add(GetOffVacationFreeCard)
    }
    
    /**
     * This is only used for testing.
     */
    override fun toString() =
        cardsQueue.joinToString(
            prefix = "Top\n",
            transform = {
                if (it !is MoveToPropertyCard) it.message
                // propPosition isn't shown in the message for MoveToPropertyCards, so show it here.
                else "${it.message} (propPosition = ${it.propPosition}"
            },
            separator = "\n",
            postfix = "\nBottom"
        )
}
