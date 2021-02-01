/**
 * ActionDeck class consists of the data and functions for a deck of action cards.
 */
class ActionDeck {

    /**
     * ActionCard class.
     *
     * @param effect is used to determine what action to take and is not displayed to the players of the game.
     * The different types and their descriptions:
     * MONEY_LOSS: Makes a player lose money.
     * MONEY_GAIN: Makes a player gain money.
     * GIVE_MONEY_TO_OTHER_PLAYERS: Requires the player that drew this card to pay money to all other players
     * that are still in the game.
     * RECEIVE_MONEY_FROM_OTHER_PLAYERS: Requires all other players that are still in the game to pay money to the
     * player that drew this card.
     * GET_OFF_VACATION_FREE: Allows the player to get off vacation for free. This card will be removed from
     * the deck and given to the player that drew it. When that player uses this card or trades it another player and
     * that player uses this card, Another card of this type gets inserted at the bottom of the deck.
     * GO_ON_VACATION: Sends the player that drew this card to vacation.
     * RELATIVE_POSITION_CHANGE: Requires the player to move positions relative to their current position.
     * For example, 4 positions ahead or 4 positions back.
     * ABSOLUTE_POSITION_CHANGE: Requires the player to a given position on the board.
     * PROPERTY_MAINTENANCE: Requires the player to pay a fee to the bank for every restaurant they own.
     *
     * @param message is the message on the cards that is displayed to the players of the game after they land on a
     * "Draw Action Card" space.
     *
     * @param value depends on the effect of card.
     * MONEY_LOSS: The amount of money the player loses.
     * MONEY_GAIN: The amount of money the player gains.
     * GIVE_MONEY_TO_OTHER_PLAYERS: The amount of money that the player must pay to every other player in the game, meaning
     * the total amount of money the player must pay is the amount of other players in the game multiplied by this value.
     * RECEIVE_MONEY_FROM_OTHER_PLAYERS: The amount of money that the player will receive from every other player in the game,
     * meaning that the total amount of money that the player receives is the amount of other players in the game
     * multiplied by this value.
     * GET_OFF_VACATION_FREE: Not used so 0.
     * GO_ON_VACATION: Not used so 0.
     * RELATIVE_POSITION_CHANGE: The amount of spaces forward the player must move. That means that if the value is
     * negative, the player moves backward.
     * ABSOLUTE_POSITION_CHANGE: The position on the board the player must move to.
     * PROPERTY_MAINTENANCE: The amount of money per restaurant that a player must pay.
     */
    data class ActionCard(val effect: Effect, val message: String, var value: Int) {
        enum class Effect {
            MONEY_LOSS,
            MONEY_GAIN,
            GIVE_MONEY_TO_OTHER_PLAYERS,
            RECEIVE_MONEY_FROM_OTHER_PLAYERS,
            GET_OFF_VACATION_FREE,
            GO_ON_VACATION,
            RELATIVE_POSITION_CHANGE,
            ABSOLUTE_POSITION_CHANGE,
            PROPERTY_MAINTENANCE
        }
    }

    private val cards = mutableListOf(
            ActionCard(
                    effect = ActionCard.Effect.ABSOLUTE_POSITION_CHANGE,
                    message = "Computer scientist Donald Knuth once said\n\'Programs are meant to be read by humans and only " +
                            "incidentally for computers to execute\'.\nMove to Knuth Street.",
                    value = 0
            ),
            ActionCard(
                    effect = ActionCard.Effect.ABSOLUTE_POSITION_CHANGE,
                    message = "Lagos is the most populated city in Africa. Move to Lagos Avenue.",
                    value = 0
            ),
            ActionCard(effect = ActionCard.Effect.MONEY_LOSS, message = "You lose $256", value = 256),
            ActionCard(effect = ActionCard.Effect.MONEY_LOSS, message = "You lose $128", value = 128),
            ActionCard(effect = ActionCard.Effect.MONEY_GAIN, message = "You receive $256", value = 256),
            ActionCard(effect = ActionCard.Effect.MONEY_GAIN, message = "You receive $128", value = 128),
            ActionCard(
                    effect = ActionCard.Effect.GIVE_MONEY_TO_OTHER_PLAYERS,
                    message = "You must pay every other player $32",
                    value = 32
            ),
            ActionCard(
                    effect = ActionCard.Effect.RECEIVE_MONEY_FROM_OTHER_PLAYERS,
                    message = "You receive $32 from every other player",
                    value = 32
            ),
            ActionCard(
                    effect = ActionCard.Effect.GET_OFF_VACATION_FREE,
                    message = "Get Off Vacation Free. Keep this card and use it when needed, or trade it.",
                    value = 0
            ),
            ActionCard(
                    effect = ActionCard.Effect.GET_OFF_VACATION_FREE,
                    message = "Get Off Vacation Free. Keep this card and use it when needed, or trade it.",
                    value = 0
            ),
            ActionCard(effect = ActionCard.Effect.GO_ON_VACATION, message = "Go On Vacation", value = 0),
            ActionCard(effect = ActionCard.Effect.GO_ON_VACATION, message = "Go On Vacation", value = 0),
            ActionCard(effect = ActionCard.Effect.RELATIVE_POSITION_CHANGE, message = "Move ahead 4 spaces", value = 4),
            ActionCard(effect = ActionCard.Effect.RELATIVE_POSITION_CHANGE, message = "Move back 4 spaces", value = -4),
            ActionCard(effect = ActionCard.Effect.RELATIVE_POSITION_CHANGE, message = "Move ahead 8 spaces", value = 8),
            ActionCard(effect = ActionCard.Effect.RELATIVE_POSITION_CHANGE, message = "Move back 8 spaces", value = -8),
            ActionCard(
                    effect = ActionCard.Effect.PROPERTY_MAINTENANCE,
                    message = "You must pay $64 per restaurant for maintenance",
                    value = 64
            ),
            ActionCard(
                    effect = ActionCard.Effect.PROPERTY_MAINTENANCE,
                    message = "You must pay $32 per restaurant for maintenance",
                    value = 32
            )
    )

    /**
     * Some of the cards require a player to move to a specific space on the board. The values of those cards are
     * dependent on the locations of those spaces on the board. A map containing these values should be passed in as
     * an argument for this function and then the values of the appropriate cards are changed. This function should be
     * called before shuffling.
     *
     * @throws NoSuchElementException if propertyPositions does not have entries whose keys are "Knuth Steet"
     * and "Lagos Avenue".
     */
    fun setPropertyPositions(propertyPositions: Map<String, Int>) {
        cards[0].value = propertyPositions.getValue("Knuth Street")
        cards[1].value = propertyPositions.getValue("Lagos Avenue")
    }

    fun shuffle() {
        cards.shuffle()
    }

    private var topIndex = 0

    /**
     * The card at the top of the deck.
     */
    val topCard get() = cards[topIndex]

    /**
     * Makes it so that the card that used to be the top card is now the bottom card and the card that used to be
     * 2nd from the top is now the top card.
     */
    fun moveTopCardToBottom() {
        // Increment topIndex and make it go back to 0 once it's equal to the amount of cards in the cards list
        topIndex = (topIndex + 1) % cards.size
    }

    /**
     * @throws IllegalStateException if the top card does not have the effect GET_OFF_VACATION_FREE.
     */
    fun removeGetOffVacationFreeCardAtTop() {
        if (topCard.effect != ActionCard.Effect.GET_OFF_VACATION_FREE) {
            throw IllegalStateException("The top card is not a \"get off vacation free\" card")
        }
        cards.removeAt(topIndex)

        // If the "Get off vacation free" card was at the end of the list, topIndex will be equal to the amount of
        // cards in the cards list and this will result in an out of bounds error the next time the top card is
        // accessed. topIndex needs to be set to 0 in this case to prevent this.
        if (topIndex == cards.size) {
            topIndex = 0
        }
    }

    fun insertGetOffVacationFreeCardAtBottom() {
        val newCard = ActionCard(
                effect = ActionCard.Effect.GET_OFF_VACATION_FREE,
                message = "Get Off Vacation Free. Keep this card and use it when needed, or trade it.",
                value = 0
        )
        if (topIndex == 0) {
            // Add to the end of the cards list
            cards.add(newCard)
        } else {
            cards.add(index = topIndex, element = newCard)
            // Increment topIndex since cards will be shifted after inserting a new one in the middle of the cards list
            topIndex++
        }
    }

    /**
     * Shows the content of the deck. Should only be used for testing and debugging purposes.
     */
    override fun toString(): String {
        val sb = StringBuilder("Top of deck\n")
        for (i in topIndex until cards.size) {
            sb.append(cards[i]).append("\n")
        }
        for (i in 0 until topIndex) {
            sb.append(cards[i]).append("\n")
        }
        sb.append("Bottom of deck")
        return sb.toString()
    }
}