class ActionDeck {
    private val cardsDeque: ArrayDeque<ActionCard> =
        listOf(
            MoneyGainCard(gainAmount = 128),
            MoneyGainCard(gainAmount = 256),
            MoneyLossCard(lossAmount = 128),
            MoneyLossCard(lossAmount = 256),
            GetMoneyFromOtherPlayersCard(moneyAmount = 32),
            GiveMoneyToOtherPlayersCard(moneyAmount = 32),
            PropertyMaintenanceCard(feePerRestaurant = 32),
            PropertyMaintenanceCard(feePerRestaurant = 64),
            RelativePositionChangeCard(moveAmount = 4),
            RelativePositionChangeCard(moveAmount = -4),
            RelativePositionChangeCard(moveAmount = 8),
            RelativePositionChangeCard(moveAmount = -8),
            LagosAvenueCard,
            KnuthStreetCard,
            GetOffVacationFreeCard,
            GetOffVacationFreeCard,
            GoOnVacationCard,
            GoOnVacationCard
        )
        .let { ArrayDeque(elements = it) }
        .apply { shuffle() }
    
    val topCard: ActionCard
        get() = cardsDeque.first()

    /**
     * Makes it so that the card that used to be the top card is now the bottom card and the card that used to be
     * 2nd from the top is now the top card.
     */
    fun moveTopCardToBottom() {
        cardsDeque.addLast(cardsDeque.removeFirst())
    }

    fun removeGetOffVacationCardAtTop() {
        if (topCard is GetOffVacationFreeCard) {
            cardsDeque.removeFirst()
        } else {
            printError(
                "The Action Deck had removeGetOffVacationCardAtTop called when the top card wasn't a " +
                "GetOffVacationFreeCard"
            )
        }
    }

    fun insertGetOffVacationCardAtBottom() {
        cardsDeque.addLast(GetOffVacationFreeCard)
    }
    
    /**
     * Shows the content of the deck. Should only be used for testing and debugging purposes.
     */
    override fun toString(): String {
        // newPosition isn't shown in the message for absolute position change cards, so show it here
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