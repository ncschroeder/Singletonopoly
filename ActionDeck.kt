package commandline


/**
 * The Entropy Deck is implemented by using a List of Entropy Cards, having an Int represent the top index,
 * using a property to access the top card, which is the element in the List at the top index,
 */
class ActionDeck() {
    /**
     * @param type is used to determine what action to take and is not displayed to the players of the game.
     * The different types and their descriptions:
     * "money loss": Makes a player lose money.
     * "money gain": Makes a player gain money.
     * "player to other players" Requires the player that drew this card to pay money to all other players
     * that are still in the game.
     * "other players to player" Requires all other players that are still in the game to pay money to the
     * player that drew this card.
     * "get off vacation free": Allows the player to get off vacation free. This card will be removed from
     * the deck until the player that drew it uses it or trades it to another player who uses it.
     * "relative position change" Requires the player to move positions relative to their current position.
     * For example, 4 positions ahead or positions back.
     * "absolute position change" Requires the player to a given position on the board.
     * "property maintenance" Requires the player to pay a fee to the bank for every restaurant they own.
     *
     * @param message is the message on the cards that is displayed to the players of the game after they land on a
     * "Draw Action Card" space.
     *
     * @param value depends on the type of card.
     * "money loss": The amount of money the player loses.
     * "money gain": The amount of money the player gains.
     * "player to other players": The amount of money that the player must pay to every other player in the game, meaning
     * the total amount of money the player must pay is the amount of other players in the game multiplied by this value.
     * "other players to player": The amount of money that the player will receive from every other player in the game,
     * meaning that the total amount of money that the player receives is the amount of other players in the game
     * multiplied by this value.
     * "get off vacation free": Not used so 0.
     * "relative position change": The amount of spaces forward the player must move. That means that if the value is
     * negative, the player moves backward.
     * "absolute position change": The position on the board the player must move to.
     * "property maintenance": The amount of money per restaurant that a player must pay.
     */
    data class ActionCard(val type: String, val message: String, var value: Int)

    private val cards = mutableListOf(
        ActionCard(
            type = "absolute position change",
            message = "Donald Knuth once said\n\'Programs are meant to be read by humans and only incidentally for " +
                    "computers to execute\'.\nMove to Knuth Street.",
            value = 0
        ),
        ActionCard(
            type = "absolute position change",
            message = "Lagos is the most populated city in Africa. Move to Lagos Avenue.",
            value = 0
        ),
        ActionCard(type = "money loss", message = "You lose $200", value = 200),
        ActionCard(type = "money loss", message = "You lose $200", value = 200),
        ActionCard(type = "money gain", message = "You receive $200", value = 200),
        ActionCard(type = "money gain", message = "You receive $200", value = 200),
        ActionCard(type = "player to other players", message = "You must pay every other player $25", value = 25),
        ActionCard(type = "other players to player", message = "You receive $25 from every other player", value = 25),
        ActionCard(type = "get off vacation free", message = "Get off vacation free", value = 0),
        ActionCard(type = "get off vacation free", message = "Get off vacation free", value = 0),
        ActionCard(type = "relative position change", message = "Move ahead 4 spaces", value = 4),
        ActionCard(type = "relative position change", message = "Move back 4 spaces", value = -4),
        ActionCard(type = "relative position change", message = "Move ahead 8 spaces", value = 8),
        ActionCard(type = "relative position change", message = "Move back 8 spaces", value = -8),
        ActionCard(
            type = "property maintenance",
            message = "You must pay $50 per restaurant for maintenance",
            value = 50
        ),
        ActionCard(
            type = "property maintenance",
            message = "You must pay $25 per restaurant for maintenance",
            value = 25
        )
    )

    /**
     * Some of the cards require a player to move to a specific space on the board. The values of those cards are
     * dependent on the locations of those spaces on the board. A map containing these values should be passed in as
     * an argument for this method and then the values of the appropriate cards are changed.
     */
    fun setPropertyPositions(propertyPositions: Map<String, Int>) {
        cards[0].value = propertyPositions["Knuth Street"]!!
        cards[1].value = propertyPositions["Lagos Avenue"]!!
    }

    fun shuffle() {
        cards.shuffle()
    }

    private var topIndex = 0

    val topCard get() = cards[topIndex]

    /**
     * Makes it so that topCard is equal to a different card the next time it's accessed.
     */
    fun moveTopCardToBottom() {
        // Make topIndex go back to the beginning once it reaches the end.
        topIndex = (topIndex + 1) % cards.size
    }

    fun removeGetOffVacationCardAtTop() {
        // This should only be called when a "get off vacation free" card is at the top.
        if (topCard.type != "get off vacation free") {
            throw IllegalStateException("The top card is not a \"get off vacation free\" card")
        }
        cards.removeAt(topIndex)

        // If the "Get off vacation free" card was at the end of the list, topIndex will be equal to the amount of
        // cards in the cards List and this will result in an out of bounds error the next time the top card is
        // accessed. topIndex needs to be set to 0 in this case to prevent this.
        if (topIndex == cards.size) {
            topIndex = 0
        }
    }

    fun insertGetOffVacationCardAtBottom() {
        val newCard = ActionCard(type = "get off vacation free", message = "Get off vacation free", value = 0)
        if (topIndex == 0) {
            // Add to the end of the cards List.
            cards.add(newCard)
        } else {
            cards.add(topIndex, newCard)
            // Increment topIndex since cards will be shifted after inserting a new one in the middle of the cards List.
            topIndex++
        }
    }

    /**
     * Prints data of the cards in the deck. This should be used only for testing and debugging purposes.
     */
    fun displayCards() {
        println("Top of deck")
        for (i in topIndex until cards.size) {
            println(cards[i])
        }
        for (i in 0 until topIndex) {
            println(cards[i])
        }
        println("Bottom of deck")
    }
}