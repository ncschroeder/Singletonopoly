package commandline

import java.lang.Exception

/**
 * @param type is used to determine what action to take and is not displayed to the players of the game.
 * The different types and their descriptions:
 * "player to bank": Requires the player to pay money to the bank.
 * "bank to player": Requires the bank to pay money to the player.
 * "get off vacation free": Allows the player to get off vacation free. This card will be removed from
 * the deck until the player that drew it uses it or trades it to another player who uses it.
 * "relative position change" Requires the player to move positions relative to their current position.
 * For example, 4 positions ahead or positions back.
 * "absolute position change" Requires the player to a given position on the board.
 * "property maintenance" Requires the player to pay a fee to the bank for every restaurant they own.
 * "player to other players" Requires the player that drew this card to pay money to all other players
 * that are still in the game.
 * "other players to player" Requires all other players that are still in the game to pay money to the
 * player that drew this card.
 *
 * @param message is the message on the cards that is displayed to the players of the game after they land on a
 * "Draw Entropy Card" space.
 *
 * @param value depends on the type of card.
 * "player to bank": The amount of money the player owes the bank.
 * "bank to player": The amount of money the bank owes the player.
 * "get off vacation free": null.
 * "relative position change": The amount of spaces forward the player must move. That means that if the value is
 * negative, the player moves backward.
 * "absolute position change": The position on the board the player must move to.
 * "property maintenance": The amount of money per restaurant that a player must pay.
 * "player to other players": The amount of money that the player must pay to every other player in the game, meaning
 * the total amount of money the player must pay is the amount of other players in the game multiplied by this value.
 * "other players to player": The amount of money that the player will receive from every other player in the game,
 * meaning that the total amount of money that the player receives is the amount of other players in the game
 * multiplied by this value.
 */
data class EntropyCard (val type: String, val message: String, val value: Int?)

class EntropyDeck {
    private val cards = mutableListOf(
        EntropyCard("player to bank", "player to bank message", 100),
        EntropyCard("bank to player", "You won a hackathon and receive $100", 100),
        EntropyCard("get off vacation free", "Get off vacation free", null),
        EntropyCard("get off vacation free", "Get off vacation free", null),
        EntropyCard("relative position change", "Move ahead 4 spaces", 4),
        EntropyCard("relative position change", "Move back 4 spaces", -4),
        EntropyCard("absolute position change",
            "Donald Knuth once said\n\'Programs are meant to be read by humans and only incidentally for " +
                    "computers to execute\'.\nMove to Knuth Street.", 3),
        EntropyCard("absolute position change", "Move to position 10", 10),
        EntropyCard("property maintenance", "You must pay $50 per restaurant for maintenance", 50),
        EntropyCard("player to other players", "player to other players message", 10),
        EntropyCard("other players to player", "other player to players message", 10)
    )

    init {
        cards.shuffle()
    }

    private var topIndex = 0

    val topCard get() = cards[topIndex]

    fun moveTopCardToBottom() {
        // Make topIndex go back to the beginning once it reaches the end.
        topIndex = (topIndex + 1) % cards.size
    }

    fun removeGetOffVacationCardAtTop() {
        // This should only be called when a "get off vacation free" card is at the top.
        if (topCard.type != "get off vacation free") {
            throw Exception("The top card is not a \"get off vacation free\" card")
        }
        cards.removeAt(topIndex)

        // If the "Get off vacation free" card was at the end of the list, topIndex will be equal to the amount of
        // cards in the cards List and this will result in an out of bounds error the next time the top card is
        // accessed. topIndex needs to be set to 0 in this case to prevent this.
        if (topIndex == cards.size) topIndex = 0
    }

    fun insertGetOffVacationCardAtBottom() {
        if (topIndex == 0) {
            // Add to the end of the cards List.
            cards.add(EntropyCard("get off vacation free", "Get off vacation free", null))
        } else {
            cards.add(topIndex, EntropyCard("get off vacation free", "Get off vacation free", null))
            // Increment topIndex since cards will be shifted after inserting a new one in the middle of the cards List.
            topIndex++
        }
    }
}