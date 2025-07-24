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
     * Makes it so that the card that used to be the top card is now the bottom card and the card that used to be
     * 2nd from the top is now the top card.
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
     * Shows the content of the deck. Should only be used for testing and debugging purposes.
     */
    override fun toString(): String {
        // newPosition isn't shown in the message for absolute position change cards, so show it here.
        fun cardTransform(c: ActionCard): String =
            c.message.plus(
                if (c is AbsolutePositionChangeCard) {
                    " (newPosition = ${c.newPosition})"
                } else ""
            )
        
        return cardsDeque.joinToString(
            prefix = "Top Of Deck\n", transform = ::cardTransform, separator = "\n", postfix = "\nBottom Of Deck"
        )
    }
}
